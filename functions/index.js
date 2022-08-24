const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();
exports.sendWelcomeEmail = functions.auth.user().onCreate((user) => {
  const email = user.email;
  if (email) {
    console.log("Sending welcome email to: " + email);
    admin.firestore().collection("mail")
        .add({
          to: email,
          message: {
            subject: "Welcome to Bren Mk Online!",
            text: "",
            html: "<html><body style=\"background-color:#EDECEC\"><table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"550\" bgcolor=\"white\" style=\"border:2px solid #FF8080\"> <tbody> <tr style=\"height: 120px; background-color: #f5f5f5 \"><td align=\"center\" style=\"border: none; padding-right: 20px;padding-left:20px\"><table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"col-550\" width=\"550\"><tbody><tr style=\"height: 120px;\"><td align=\"center\" height: 50px;\"><img src=\"https://firebasestorage.googleapis.com/v0/b/bren-mk-android.appspot.com/o/assets%2Fbren-mk-logo4.png?alt=media&token=7bd93d11-269b-46cc-9409-13fba679b576\" style=\"height: 100px; width:300px\"/></td></tr></tbody></table></td></tr><tr style=\"height: 40px;\"><td align=\"center\" style=\"border: none;padding-right: 20px;padding-left:20px\"><p style=\"font-weight: bolder;font-size: 32px; font-family: 'Trebuchet MS', sans-serif; letter-spacing: 0.025em; color: #FF8080\">Welcome to Bren Mk Online App!</p></td></tr><tr style=\"height: 400px;\"><td ><p style=\"font-size: 16px; font-family: Garamond, serif;letter-spacing: 0.025em; padding: 10;text-align: center;color:#383838;\">You are receiving this email because you recently signed up in our Application. You must be receiving an email in the next couple of minutes to verify your account.</p><p style=\"font-size: 16px; font-family: Garamond, serif;letter-spacing: 0.025em; padding: 10;text-align: center;color:#383838;\">If you don't find it in your inbox, please check your junk folder. </p><br /><br /><br /><br /><br /><br /><br /><p style=\"font-size: 16px; font-family: Garamond, serif;text-align: center;letter-spacing: 0.025em; padding: 10;color:#383838;\">Wasn't it you? <a href=\"#\">please let us know.</a></p></td></tr><tr style=\"border: none; background-color: #f5f5f5; height: 40px; color:white; padding-bottom: 20px; text-align: center;\"><td height=\"40px\" align=\"center\"><p style=\"color:#383838; font-family: 'Brush Script MT', cursive;font-size: 32px;line-height: 1.5em;\">Bren Mk Online</p><p style=\"font-family:'Open Sans', Arial, sans-serif;font-size:11px; line-height:18px; color:#383838;\">© 2022 Bren Mk Online. All Rights Reserved.</p></td></tr></tbody></table></body></html>",
          },
        }).then(() => console.log("Queued email for delivery!"));
    return 200;
  } else {
    console.log("No email found. Anonymous user");
    return 400;
  }
});
