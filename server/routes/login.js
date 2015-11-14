var express = require('express');
var router = express.Router();
var async = require('async');
var jwt = require('jsonwebtoken');
var User = require('../models/user.js');
var config = require('../config.js');

router.post('/', function(req, res) {
    req.checkBody('username').notEmpty();
    req.checkBody('password').notEmpty();
    var errors = req.validationErrors();
    if (errors) {
        res.status(400).json({ success: false, error: errors });
        return;
    }

    async.waterfall([
        function(callback) {
            User.findOne({ username: req.body.username }, callback);
        },
        function(user, callback) {
            if (!user) return callback(1);
            user.comparePassword(req.body.password, function(err, isMatch) {
                callback(err, isMatch, user);
            });
        }
    ], function(err, isMatch, user) {
        if (err || isMatch === false)
            res.status(200).json({ success: false, error: 'Wrong username or password' });
        else {
            var msg = {
                userId: user._id
            };
            var token = jwt.sign(msg, config.secret_jwt, { expiresIn: '1h' });
            res.status(200).json({ success: true, token: token });
        }
    });
});

module.exports = router;
