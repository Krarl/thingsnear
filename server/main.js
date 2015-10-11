var express = require('express');
var app = express();
var bodyParser = require('body-parser');
var mongoose = require('mongoose');
var async = require('async');
var expressValidator = require('express-validator');
var logging = require('./helpers/logging.js');
var auth = require('./middleware/auth.js');

//Databas
//config.autoIndex = false;
mongoose.connect('mongodb://127.0.0.1:27017', function(err) {
    if (err)
        throw err;
});

//låter oss få data från en POST
app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());
app.use(expressValidator());

var port = process.env.OPENSHIFT_NODEJS_PORT || 8080;
var ip = process.env.OPENSHIFT_NODEJS_IP || '127.0.0.1';

//logga alla requests
app.use('/', require('./middleware/logging.js'));

//Routes
app.use('/feed', auth.verify, require('./routes/feed.js'));
app.use('/login', require('./routes/login.js'));
app.use('/test', auth.verify, require('./routes/test.js'));
app.use('/users', require('./routes/users.js'));

//starta servern
app.listen(port);
logging.log('Server started on port ' + port);
