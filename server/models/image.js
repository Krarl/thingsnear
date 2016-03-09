var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var ImageSchema = new Schema({
    creator: { type: Schema.Types.ObjectId, ref: 'User', required: false },
    date: { type: Date, default: Date.now },
});

module.exports = mongoose.model('Image', ImageSchema);
