var express = require('express');
var app = express();
var bodyParser = require('body-parser');
var mongoose = require('mongoose');

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
mongoose.connect('mongodb://127.0.0.1:27017');

//låter oss få data från en POST
app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());

var port = process.env.OPENSHIFT_NODEJS_PORT || 8080;
var ip = process.env.OPENSHIFT_NODEJS_IP || '127.0.0.1';

//Routes
var router = express.Router();
router.get('/test', function(req,  res) {
    res.status(200).json({ message: 'dumdididumdidum!' });
});

app.use('/', router);

//Starta servern
app.listen(port);
console.log('Server started on port ' + port);
