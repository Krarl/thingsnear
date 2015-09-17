var express = require('express');
var app = express();
var bodyParser = require('body-parser');
var mongoose = require('mongoose');
var async = require('async');

//små fina funktioner
function getPrettyTime() {
    function fix(num) {
        if (num.length == 1)
            return '0' + num;
        else
            return num;
    }

    var now = new Date();
    return now.getUTCFullYear().toString() + '-' + fix(now.getUTCMonth().toString()) + '-' + fix(now.getUTCDate().toString())
     + ' ' + fix(now.getUTCHours().toString()) + ':' + fix(now.getUTCMinutes().toString()) + ':' + fix(now.getUTCSeconds().toString())
     + ' UTC';
}

function log(text) {
    console.log(getPrettyTime() + ': ' + text);
}

//Databas
//config.autoIndex = false;
mongoose.connect('mongodb://127.0.0.1:27017');
var User = require('./models/user.js');

//låter oss få data från en POST
app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());

var port = process.env.OPENSHIFT_NODEJS_PORT || 8080;
var ip = process.env.OPENSHIFT_NODEJS_IP || '127.0.0.1';

//Routes
var router = express.Router();

router.use(function(req, res, next) {
    log('Recieved request for ' + req.path);
    next(); //gå vidare till nästa handler
});

router.get('/test', function(req,  res) {
    res.status(200).json({ message: 'dumdididumdidum!' });
});

router.route('/users')
    .post(function(req, res) {
        var user = new User();
        user.name = req.body.name;
        user.save(function(err) {
            if (err)
                res.send(err);
            res.json({ message: 'User created' });
        });
    })
    .get(function(req, res) {
        User.find(function(err, users) {
            if (err)
                res.send(err);
            res.json(users);
        });
    });

router.route('/users/:id')
    .get(function(req, res) {
        User.findById(req.params.id, function(err, user) {
            if (err)
                res.send(err);
            res.json(user);
        });
    })
    .put(function(req, res) {
        User.findById(req.params.id, function(err, user) {
            if (err)
                res.send(err);
            user.name = req.body.name;
            user.save(function (err) {
                if (err)
                    res.send(err);

                res.json({ message: 'User updated' });
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
                res.json({ message: 'User deleted' });
            });
        });
    });

app.use('/', router);

//Starta servern
app.listen(port);
console.log('Server started on port ' + port);
