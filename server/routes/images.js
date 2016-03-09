var express = require('express');
var router = express.Router();
var fs = require('fs');
var config = require('../config.js');
var log = require('../helpers/logging.js');
var auth = require('../middleware/auth.js');
var Image = require('../models/image.js');
var async = require('async');
var multer = require('multer');
var upload = multer({ dest: config.image_dir + '/', limits: { fileSize: 1024*1024 * 10, files: 1} });

router.route('/')
    .get(function(req, res) {
        Image.find(function(err, images) {
            if (err) {
                res.json({success: false, error: 'Database error' });
            } else
                res.json({ success: true, images: images });
        });
    })

    //.post(auth.verify)
    .post(function(req, res) {
        //skapa först bilden i databasen
        //spara den sen i en imagemapp med id från databasen

        async.waterfall([
            //tar emot filen
            function(callback) {
                upload.single('image')(req, res, function(err) {
                    if (err) {
                        log.error('Multer error: ' + err);
                        callback('Error recieving file');
                    } else {
                        callback(null);
                    }
                });
            },
            //kontrollerar vad vi tagit emot
            function(callback) {
                if (!req.file) {
                    log.error('No image recieved');
                    return callback('No file recieved');
                }
                //här borde vi egentligen också kolla så det faktiskt är en bild som laddas upp och inte en exe
                return callback(null);
            },
            //skapar en entry i databasen
            function(callback) {
                var image = new Image();
                image.creator = req.user._id;
                image.mimetype = req.file.mimetype;
                image.filesize = req.file.size;

                image.save(function(err) {
                    if (err) {
                        log.error('Error on create image: ' + err);
                        return callback('Database error');
                    } else {
                        return callback(null, image._id);
                    }
                });
            },
            //döper om filen till det databasen kallar den
            function(id, callback) {
                fs.rename(req.file.path, config.image_dir + '/' + id, function(err) {
                    if (err) {
                        log.error('Error saving image: ' + err);
                        return callback('Unknkown error saving image');
                    } else {
                        return callback(null);
                    }
                });
            }
        ], function(err) {
            if (err) {
                //tar bort filen, om den skapats
                fs.unlink(req.file.path, function(err) {});
                res.status(500).json({ success: false, error: err });
            } else {
                res.status(200).json({ success: true });
            }
        });
    });

router.get('/:id', function(req, res) {
    async.waterfall([
        //hämtar den från databasen
        function(callback) {
            Image.findById(req.params.id, function(err, image) {
                if (err | !image) {
                    log.error('Database error: ' + err);
                    callback(err);
                } else {
                    callback(null, image);
                }
            });
        },
        function(image, callback) {
            var options = {
                root: config.image_dir + '/',
                headers: { 'Content-Type': image.mimetype }
            };
            res.sendFile(image._id, options, function(err) {
                if (err) {
                    log.error(err);
                    callback('File not found');
                } else {
                    callback(null);
                }
            });
        }
    ], function(err) {
        if (err) {
            res.status(404).json({ success: false, error: err });
        }
    });
});

module.exports = router;
