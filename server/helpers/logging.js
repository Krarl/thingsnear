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

exports.log = function(text) {
    console.log(getPrettyTime() + ': ' + text);
};
