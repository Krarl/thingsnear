var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var postSchema = new Schema({
    //user: { type: Schema.ObjectId, ref: 'User' },
    location: {
        type: {
            type: String,
            default: 'Point'
        },
        coordinates: [Number],
    },
    date: { type: Date, default: Date.now },
    content: String
});

postSchema.index({ location: '2dsphere' });

module.exports = mongoose.model('Post', postSchema);
