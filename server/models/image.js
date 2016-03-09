var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var ImageSchema = new Schema({
    creator: { type: Schema.Types.ObjectId, ref: 'User', required: true },
    date: { type: Date, default: Date.now },
    filesize: { type: Number, required: true },
    mimetype: { type: String, required: true }
});

module.exports = mongoose.model('Image', ImageSchema);
