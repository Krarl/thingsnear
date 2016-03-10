module.exports = {
    secret_jwt: "sldgkjsoiunvnei213214",
    server_ip: process.env.OPENSHIFT_NODEJS_IP || undefined,
    server_port: process.env.OPENSHIFT_NODEJS_PORT || 8080,
    mongo_connection_string: 'mongodb://127.0.0.1:27017',
    image_dir: 'C:/Users/edu97250/Desktop/images/'
};

//Are we running on openshift? Then use the appropiate mongo variables and directory folder
if (process.env.OPENSHIFT_MONGODB_DB_PASSWORD) {
    module.exports.mongo_connection_string =
        process.env.OPENSHIFT_MONGODB_DB_USERNAME + ":" +
        process.env.OPENSHIFT_MONGODB_DB_PASSWORD + "@" +
        process.env.OPENSHIFT_MONGODB_DB_HOST + ':' +
        process.env.OPENSHIFT_MONGODB_DB_PORT + '/' +
        process.env.OPENSHIFT_APP_NAME;

    module.exports.image_dir = process.env.OPENSHIFT_DATA_DIR + '/images/';
}
