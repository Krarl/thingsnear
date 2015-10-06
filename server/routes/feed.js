var express = require('express');
var router = express.Router();
var Post = require('../models/post.js');

router.route('/')
    .post(function(req, res) {
        req.checkBody('content').notEmpty();
        req.checkBody('latitude').isDecimal();
        req.checkBody('longitude').isDecimal();
        var errors = req.validationErrors();
        if (errors) {
            res.status(400).json({ success: false, errors: errors });
            return;
        }

        var post = new Post();
        post.content = req.body.content;
        post.location.coordinates = [req.body.longitude, req.body.latitude];
        post.creator = req.user._id;
        post.save(function(err) {
            if (err)
                res.send(err);
            res.json({ success: true });
        });
    })
    .get(function(req, res) {
        req.checkQuery('latitude').isDecimal();
        req.checkQuery('longitude').isDecimal();
        var errors = req.validationErrors();
        if (errors) {
            res.status(400).json({ success: false, errors: errors });
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
