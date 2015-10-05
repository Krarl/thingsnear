var express = require('express');
var router = express.Router();
var Post = require('../models/post.js');

router.route('/')
    .post(function(req, res) {
        var post = new Post();
        post.content = req.body.content;
        post.location.coordinates = [req.body.longitude, req.body.latitude];
        post.creator = req.user._id;
        post.save(function(err) {
            if (err)
                res.send(err);
            res.json({ message: 'Post created' });
        });
    })
    .get(function(req, res) {
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
            res.json(posts);
        });
    });

module.exports = router;
