var jwt = require('jsonwebtoken');
var User = require('../models/user.js');
var config = require('../config.js');

exports.verify = function(req, res, next) {
    var token = req.body.token || req.query.token || req.headers['x-access-token'];

    if (token) {
        jwt.verify(token, config.secret_jwt, function(err, decoded) {
            if (err) {
                res.status(403).json({ success: false, error: 'Invalid login token' });
            } else {
                User.findById(decoded.userId, function(err, user) {
                    if (err)
                        res.status(403).json({ success: false, error: 'User cannot be found' });
                    else {
                        req.user = user;
                        next();
                    }
                });
            }
        });
    } else {
        res.status(403).json({ success: false, error: 'No login token provided' });
    }
};
