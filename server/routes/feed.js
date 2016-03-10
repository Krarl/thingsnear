var express = require('express');
var router = express.Router();
var Post = require('../models/post.js');
var log = require('../helpers/logging.js');

router.route('/')
    .post(function(req, res) {
        req.checkBody('latitude').isDecimal();
        req.checkBody('longitude').isDecimal();
        var errors = req.validationErrors();
        if (!req.body.content && !req.body.image) {
            errors.push('Post needs content');
        }

        if (errors) {
            res.status(400).json({ success: false, error: errors });
            return;
        }

        var post = new Post();
        if (req.body.content) {
            post.content = req.body.content;
        }
        if (req.body.image) {
            post.image = req.body.image;
        }
        post.location.coordinates = [req.body.longitude, req.body.latitude];
        post.creator = req.user._id;
        post.save(function(err) {
            if (err) {
                log.error('Error saving post: ' + err);
                res.json({ success: false, error: 'Error saving post' });
            } else {
                res.json({ success: true });
            }
        });
    })
    .get(function(req, res) {
        req.checkQuery('latitude').isDecimal();
        req.checkQuery('longitude').isDecimal();
        var errors = req.validationErrors();
        if (errors) {
            res.status(400).json({ success: false, error: errors });
            return;
        }

        Post.find({
            location: { $near: {
                $geometry: {
                    type: "Point" ,
                    coordinates: [req.query.longitude , req.query.latitude]
                },
                $maxDistance: 10000
            }}
        })
        .skip(req.query.skip)
        .sort({ date: -1 })
        .populate('creator', '-password')
        .exec(function(err, posts) {
            if (err)
                res.send(err);
            res.json({ success: true, posts: posts });
        });
    });

module.exports = router;
