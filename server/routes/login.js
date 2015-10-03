var express = require('express');
var router = express.Router();
var User = require('../models/user.js');
var async = require('async');

router.post('/', function(req, res) {
    async.waterfall([
        function(callback) {
            User.findOne({ username: req.body.username }, callback);
        },
        function(user, callback) {
            if (!user) return callback(1);
            user.comparePassword(req.body.password, callback);
        }
    ], function(err, isMatch) {
        if (err || isMatch === false)
            res.status(401).set('WWW-Authenticate', 'None').send();
        else
            res.status(200).send('du är inloggad');
    });
});

module.exports = router;
