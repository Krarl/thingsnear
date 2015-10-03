var logging = require('../helpers/logging.js');

module.exports = function(req, res, next) {
    logging.log('Recieved ' + req.method + ' request for ' + req.path);
    next(); //gå vidare till nästa handler
}
