var express = require('express');
var router = express.Router();
var User = require('../models/user.js');
var auth = require('../middleware/auth.js');

//Alla användare
router.route('/')
    .post(function(req, res) {
        //verifierar parametrar
        req.checkBody('username').notEmpty();
        req.checkBody('email').isEmail();
        req.checkBody('password').notEmpty();
        var errors = req.validationErrors();
        if (errors) {
            res.json({ success: false, error: errors });
            return;
        }

        //skapar användaren
        var user = new User();
        user.username = req.body.username;
        user.password = req.body.password;
        user.email = req.body.email;

        user.save(function(err) {
            if (err)
                res.json({ success: false, error: "Database error" });
            else
                res.json({ success: true });
        });
    })
    .get(function(req, res) {
        User.find(function(err, users) {
            if (err)
                res.send(err);
            res.json(users);
        });
    })
    .delete(function(req, res) {
        //tar bort alla användare
        User.remove({}, function(err) {
            if (err) res.send(err);
            res.json({ message: 'All users deleted, you monster' });
        });
    });

//En speciell användare
router.route('/:id', auth.verify)
    .get(function(req, res) {
        User.findById(req.params.id, function(err, user) {
            if (err || !user)
                res.status(404).json({ success: false });
            else
                res.json({ success: true, user: user });
        });
    })
    .put(function(req, res) {
        if (req.user._id != id) {
            res.json({ success: false, error: 'Cannot edit other user' });
        }

        User.findById(req.params.id, function(err, user) {
            if (err)
                res.send(err);
            user.name = req.body.name;
            user.save(function (err) {
                if (err)
                    res.send(err);

                res.json({ success: true, message: 'User updated' });
            });
        });
    })
    .delete(function(req, res) {
        User.findById(req.params.id, function(err, user) {
            if (err)
                res.send(err);
            user.remove(function (err) {
                if (err)
                    res.send(err);
                res.json({ success: true, message: 'User deleted' });
            });
        });
    });

module.exports = router;
