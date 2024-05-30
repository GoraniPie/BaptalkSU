/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.sendChatNotification = functions
    .database.ref("/messages/{roomId}/{messageId}")
    .onCreate(async (snapshot, context) => {
      const message = snapshot.val();
      const roomId = context.params.roomId;

      // Get the room details
      const roomSnapshot = await admin.database().ref(`/chatRooms/${roomId}`)
          .once("value");
      const roomData = roomSnapshot.val();

      if (!roomData) {
        return null;
      }

      // Get the tokens of the users in the room
      const userTokens = [];
      for (const userId in roomData.users) {
        if (userId !== message.senderId) {
          const userSnapshot = await admin.database()
              .ref(`/users/${userId}/fcmToken`).once("value");
          const token = userSnapshot.val();
          if (token) {
            userTokens.push(token);
          }
        }
      }

      if (userTokens.length > 0) {
        const payload = {
          notification: {
            title: `New message in ${roomData.roomName}`,
            body: message.message,
            clickAction: "FLUTTER_NOTIFICATION_CLICK",
          },
        };

        return admin.messaging().sendToDevice(userTokens, payload);
      }

      return null;
    });

