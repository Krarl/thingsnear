var mongoose = require('mongoose');
var Schema = mongoose.Schema;
var bcrypt = require('bcrypt');
var saltWorkFactor = 10; //standardvärde för bcrypt är 10

var UserSchema = new Schema({
    username: { type: String, required: true, index: {unique: true} },
    password: { type: String, required: true },
    tokens: [String]
});

//vi vill hasha lösenord innan vi sparar dem i databasen
UserSchema.pre('save', function(next) {
    var user = this;

    //har vi inte satt ett nytt lösenord finns ingen anledning att hasha det
    if (!user.isModified('password'))
        return next();

    bcrypt.genSalt(saltWorkFactor, function(err, salt) {
        if (err) return next(err);
        bcrypt.hash(user.password, salt, function(err, hash) {
            if (err) return next(err);
            user.password = hash; //skriv över klartext-lösenordet
            next();
        });
    });
});

UserSchema.methods.comparePassword = function(pass, callback) {
    bcrypt.compare(pass, this.password, function(err, isMatch) {
        if (err) return callback(err);
        callback(null, isMatch);
    });
};

module.exports = mongoose.model('User', UserSchema);
