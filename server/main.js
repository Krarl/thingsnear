var express = require('express');
var app = express();
var bodyParser = require('body-parser');
var mongoose = require('mongoose');
var async = require('async');
var expressValidator = require('express-validator');
var logging = require('./helpers/logging.js');
var auth = require('./middleware/auth.js');
var config = require('./config.js');

//Databas
//config.autoIndex = false;
logging.log('Connecting to database at ' + config.mongo_connection_string);
mongoose.connect(config.mongo_connection_string, function(err) {
    if (err) {
        logging.log('Error connecting to database');
        throw err;
    } else {
        logging.log('Connected to database');

        //låter oss få data från en POST
        app.use(bodyParser.urlencoded({ extended: true }));
        app.use(bodyParser.json());

        //låter oss validera parametrar
        app.use(expressValidator());

        //redirecta http till https
        app.use('/', require('./middleware/https.js'));

        //logga alla requests
        app.use('/', require('./middleware/logging.js'));

        //Routes
        app.use('/feed', auth.verify, require('./routes/feed.js'));
        app.use('/login', require('./routes/login.js'));
        app.use('/test', auth.verify, require('./routes/test.js'));
        app.use('/users', require('./routes/users.js'));
        app.use('/images', require('./routes/images.js'));

        var ip = config.server_ip;
        var port = config.server_port;

        //starta servern
        if (ip !== undefined) {
            app.listen(port, ip);
            logging.log('Server started on port ' + port + ' and host ' + ip);
        } else {
            app.listen(port);
            logging.log('Server started on port ' + port);
        }
    }
});
