var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var PostSchema = new Schema({
    creator: { type: Schema.Types.ObjectId, ref: 'User', required: true },
    location: {
        type: {
            type: String,
            default: 'Point'
        },
        coordinates: [Number],
    },
    date: { type: Date, default: Date.now },
    content: { type: String, required: true }
});

PostSchema.index({ location: '2dsphere' });

module.exports = mongoose.model('Post', PostSchema);
