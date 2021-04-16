const functions = require('firebase-functions');

const admin = require('firebase-admin');
const { database, messaging } = require('firebase-admin');
admin.initializeApp();

exports.pushNotifications = functions.database.ref('messages/{userId}/{sendersId}/{messageId}').onCreate((snapShot,context) => {

    const message = snapShot.val();
    const fromUser = message['fromUserName']

    const notificationBody = message['text'];
    const payload = {
        notification: {
            title : fromUser,
            body : notificationBody,
            sound : "default",
            action : "ChatActivity"
        }
    };

    const options = {
        priority : "high",
        timeToLive: 60*60*24
    };

    return admin.messaging().sendToTopic("pushNotifications",payload,options);
});