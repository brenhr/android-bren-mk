const functions = require("firebase-functions");
const admin = require("firebase-admin");
const cors = require("cors")({origin: true});
const express = require("express");
const https = require("https");

const stripe = require("stripe")(functions.config().stripe.secret, {
  apiVersion: "2022-08-01",
});

const app = express();
app.use(cors);

admin.initializeApp();
const firestore = admin.firestore();
exports.sendWelcomeEmail = functions.auth.user().onCreate((user) => {
  const email = user.email;
  if (email) {
    console.log("Sending welcome email to: " + email);
    const subject = "Welcome to Bren Mk Online!";
    const template = "<html><body style=\"background-color:#EDECEC\"><table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"550\" bgcolor=\"white\" style=\"border:2px solid #FF8080\"> <tbody> <tr style=\"height: 120px; background-color: #f5f5f5 \"><td align=\"center\" style=\"border: none; padding-right: 20px;padding-left:20px\"><table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"col-550\" width=\"550\"><tbody><tr style=\"height: 120px;\"><td align=\"center\"\"><img src=\"https://firebasestorage.googleapis.com/v0/b/bren-mk-android.appspot.com/o/assets%2Fbren-mk-logo4.png?alt=media&token=7bd93d11-269b-46cc-9409-13fba679b576\" style=\"height: 100px; width:300px\"/></td></tr></tbody></table></td></tr><tr style=\"height: 40px;\"><td align=\"center\" style=\"border: none;padding-right: 20px;padding-left:20px\"><p style=\"font-weight: bolder;font-size: 32px; font-family: 'Trebuchet MS', sans-serif; letter-spacing: 0.025em; color: #FF8080\">Welcome to Bren Mk Online App!</p></td></tr><tr style=\"height: 400px;\"><td ><p style=\"font-size: 16px; font-family: Garamond, serif;letter-spacing: 0.025em; padding: 10;text-align: center;color:#383838;\">You are receiving this email because you recently signed up in our Application. You must be receiving an email in the next couple of minutes to verify your account.</p><p style=\"font-size: 16px; font-family: Garamond, serif;letter-spacing: 0.025em; padding: 10;text-align: center;color:#383838;\">If you don't find it in your inbox, please check your junk folder. </p><br /><br /><br /><br /><br /><br /><br /><p style=\"font-size: 16px; font-family: Garamond, serif;text-align: center;letter-spacing: 0.025em; padding: 10;color:#383838;\">Wasn't it you? <a href=\"#\">please let us know.</a></p></td></tr><tr style=\"border: none; background-color: #f5f5f5; height: 40px; color:white; padding-bottom: 20px; text-align: center;\"><td height=\"40px\" align=\"center\"><p style=\"color:#383838; font-family: 'Brush Script MT', cursive;font-size: 32px;line-height: 1.5em;\">Bren Mk Online</p><p style=\"font-family:'Open Sans', Arial, sans-serif;font-size:11px; line-height:18px; color:#383838;\">© 2022 Bren Mk Online. All Rights Reserved.</p></td></tr></tbody></table></body></html>";
    sendEmail(email, subject, template);
    return 200;
  } else {
    console.log("No email found. Anonymous user");
    return 400;
  }
});

exports.sendOrderStatusMail = functions.firestore
    .document("users/{userId}/orders/{orderId}")
    .onUpdate((change, context) => {
      const newValue = change.after.data();
      const userId = context.params.userId;
      const orderId = context.params.orderId;
      const trackingNumber = newValue.trackingNumber;

      return firestore.collection("users").doc(userId).get().then((doc) => {
        if (doc.exists) {
          const email = doc.data().email;
          let template = "";
          let subject = "";
          if (newValue.status === "confirmed") {
            console.log("Sending confirmation email to user:  " + email);
            subject = "Order confirmation";
            template = "<html><body style=\"background-color:#EDECEC\"><table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"550\" bgcolor=\"white\" style=\"border:2px solid #FF8080\"><tbody><tr style=\"height: 120px; background-color: #f5f5f5 \"><td align=\"center\" style=\"border: none; padding-right: 20px;padding-left:20px\"><table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"col-550\" width=\"550\"><tbody><tr style=\"height: 120px;\"><td align=\"center\"\"><img src=\"https://firebasestorage.googleapis.com/v0/b/bren-mk-android.appspot.com/o/assets%2Fbren-mk-logo4.png?alt=media&token=7bd93d11-269b-46cc-9409-13fba679b576\" style=\"height: 100px; width:300px\"/></td></tr></tbody></table></td></tr><tr style=\"height: 40px;\"><td align=\"center\" style=\"border: none;padding-right: 20px;padding-left:20px\"><p style=\"font-weight: bolder;font-size: 32px; font-family: 'Trebuchet MS', sans-serif; letter-spacing: 0.025em; color: #FF8080\">Your order has been confirmed!</p></td></tr><tr style=\"height: 400px;\"><td ><p style=\"font-size: 16px; font-family: Garamond, serif;letter-spacing: 0.025em; padding: 10; color:#383838;\">Thanks for trusting Bren Mk Online! Your payment has been successfuly processed and your order has been confirmed. Below you will find your order details:</p><p style=\"font-size: 16px; font-family: Garamond, serif;letter-spacing: 0.025em; padding: 10; color:#383838;\">Order ID: <strong>" + orderId + "</strong></p><br /><br /><br /><br /><br /><br /><br /><br /><p style=\"font-size: 16px; font-family: Garamond, serif;text-align: center;letter-spacing: 0.025em; padding: 10;color:#383838;\">Any questions? <a href=\"#\">Contact us</a></p></td></tr><tr style=\"border: none; background-color: #f5f5f5; height: 40px; color:white; padding-bottom: 20px; text-align: center;\"><td height=\"40px\" align=\"center\"><p style=\"color:#383838; font-family: 'Brush Script MT', cursive;font-size: 32px;line-height: 1.5em;\">Bren Mk Online</p><p style=\"font-family:'Open Sans', Arial, sans-serif;font-size:11px; line-height:18px; color:#383838;\">© 2022 Bren Mk Online. All Rights Reserved.</p></td></tr></tbody></table></body></html>";
          } else if (newValue.status === "on-the-way") {
            console.log("Sending tracking email to user:  " + email);
            subject = "Your order is on the way!";
            template = "<html><body style=\"background-color:#EDECEC\"><table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"550\" bgcolor=\"white\" style=\"border:2px solid #FF8080\"><tbody><tr style=\"height: 120px; background-color: #f5f5f5 \"><td align=\"center\" style=\"border: none; padding-right: 20px;padding-left:20px\"><table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"col-550\" width=\"550\"><tbody><tr style=\"height: 120px;\"><td align=\"center\"\"><img src=\"https://firebasestorage.googleapis.com/v0/b/bren-mk-android.appspot.com/o/assets%2Fbren-mk-logo4.png?alt=media&token=7bd93d11-269b-46cc-9409-13fba679b576\" style=\"height: 100px; width:300px\"/></td></tr></tbody></table></td></tr><tr style=\"height: 40px;\"><td align=\"center\" style=\"border: none;padding-right: 20px;padding-left:20px\"><p style=\"font-weight: bolder;font-size: 32px; font-family: 'Trebuchet MS', sans-serif; letter-spacing: 0.025em; color: #FF8080\">Your order is on the way!</p></td></tr><tr style=\"height: 400px;\"><td ><p style=\"font-size: 16px; font-family: Garamond, serif;letter-spacing: 0.025em; padding: 10; color:#383838;\">Good news! Your order <strong>"+ orderId + "</strong> has been shipped. Below you can find more details regarding the carrier and tracking information:</p><p style=\"font-size: 16px; font-family: Garamond, serif;letter-spacing: 0.025em; padding: 10; color:#383838;\">Carrier: <a href=\"#\">DeliveryFast.com</a></p><p style=\"font-size: 16px; font-family: Garamond, serif;letter-spacing: 0.025em; padding: 10; color:#383838;\">Tracking Number: <strong>" + trackingNumber + "</strong></p><br /><br /><br /><br /><br /><br /><br /><br /><p style=\"font-size: 16px; font-family: Garamond, serif;text-align: center;letter-spacing: 0.025em; padding: 10;color:#383838;\">Any questions? <a href=\"#\">Contact us</a></p></td></tr><tr style=\"border: none; background-color: #f5f5f5; height: 40px; color:white; padding-bottom: 20px; text-align: center;\"><td height=\"40px\" align=\"center\"><p style=\"color:#383838; font-family: 'Brush Script MT', cursive;font-size: 32px;line-height: 1.5em;\">Bren Mk Online</p><p style=\"font-family:'Open Sans', Arial, sans-serif;font-size:11px; line-height:18px; color:#383838;\">© 2022 Bren Mk Online. All Rights Reserved.</p></td></tr></tbody></table></body></html>";
          } else if (newValue.status === "delivered") {
            console.log("Sending delivery email to user:  " + email);
            subject = "Yoir order has been delivered!";
            template = "<html><body style=\"background-color:#EDECEC\"><table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"550\" bgcolor=\"white\" style=\"border:2px solid #FF8080\"><tbody><tr style=\"height: 120px; background-color: #f5f5f5 \"><td align=\"center\" style=\"border: none; padding-right: 20px;padding-left:20px\"><table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"col-550\" width=\"550\"><tbody><tr style=\"height: 120px;\"><td align=\"center\"><img src=\"https://firebasestorage.googleapis.com/v0/b/bren-mk-android.appspot.com/o/assets%2Fbren-mk-logo4.png?alt=media&token=7bd93d11-269b-46cc-9409-13fba679b576\" style=\"height: 100px; width:300px\"/></td></tr></tbody></table></td></tr><tr style=\"height: 40px;\"><td align=\"center\" style=\"border: none;padding-right: 20px;padding-left:20px\"><p style=\"font-weight: bolder;font-size: 32px; font-family: 'Trebuchet MS', sans-serif; letter-spacing: 0.025em; color: #FF8080\">Your order has been delivered!</p></td></tr><tr style=\"height: 400px;\"><td ><p style=\"font-size: 16px; font-family: Garamond, serif;letter-spacing: 0.025em; padding: 10; color:#383838;\">Good news! <a href=\"#\">DeliveryFast.com</a> confirmed that the following order has been delivered:</p><p style=\"font-size: 16px; font-family: Garamond, serif;letter-spacing: 0.025em; padding: 10; color:#383838;\">Order ID: <strong>" + orderId + "</strong></p><p style=\"font-size: 16px; font-family: Garamond, serif;letter-spacing: 0.025em; padding: 10; color:#383838;\">We hope you enjoy your clothes. Thanks for choosing our products, and we hope to hear from you anytime soon!</p><br /><br /><br /><br /><br /><br /><br /><br /><p style=\"font-size: 16px; font-family: Garamond, serif;text-align: center;letter-spacing: 0.025em; padding: 10;color:#383838;\">Any questions? <a href=\"#\">Contact us</a></p></td></tr><tr style=\"border: none; background-color: #f5f5f5; height: 40px; color:white; padding-bottom: 20px; text-align: center;\"><td height=\"40px\" align=\"center\"><p style=\"color:#383838; font-family: 'Brush Script MT', cursive;font-size: 32px;line-height: 1.5em;\">Bren Mk Online</p><p style=\"font-family:'Open Sans', Arial, sans-serif;font-size:11px; line-height:18px; color:#383838;\">© 2022 Bren Mk Online. All Rights Reserved.</p></td></tr></tbody></table></body></html>";
          } else {
            console.log("Invalid status:  " + newValue.status);
            return 400;
          }
          return sendEmail(email, subject, template);
        } else {
          console.log("Error when finding users information");
          return 500;
        }
      }).catch((reason) => {
        console.log(reason);
        return 500;
      });
    });

exports.scheduledFunctionCrontab = functions.pubsub.schedule("*/5 0 * * *")
    .timeZone("America/New_York")
    .onRun((context) => {
      let trackingURL = "https://delivery-fast-tracking.free.beeceptor.com/tracking/";
      console.log("Checking shipping status for orders with status on-the-way");
      const ordersRef = firestore.collectionGroup("orders")
          .where("status", "==", "on-the-way");
      return ordersRef.get().then((snap) => {
        snap.forEach((doc) => {
          console.log("Checking tracking status for order ID: " + doc.id);
          const trackingNumber = doc.data().trackingNumber;
          trackingURL = trackingURL + trackingNumber;
          https.get(trackingURL, (resp) => {
            let data = "";
            resp.on("data", (chunk) => {
              data += chunk;
            });
            resp.on("end", () => {
              const result = JSON.parse(data);
              if (result.status === "DL") {
                doc.ref.update({
                  status: "delivered",
                }).then(() => {
                  console.log("Order successfully updated!");
                  return 200;
                }).catch((error) => {
                  console.error("Error updating order :" + error.message);
                  return 500;
                });
              }
            });
          }).on("error", (err) => {
            console.log("Error: " + err.message);
            return 500;
          });
        });
      });
    });

app.post("/changeOrderStatus", (req, res) => {
  const orderId = req.query.orderId;
  const userId = req.query.userId;
  const status = req.query.status;
  const trackingNumber = req.query.trackingNumber;

  const orderRef = firestore.collection("users").doc(userId)
      .collection("orders").doc(orderId);
  return orderRef.update({
    status: status,
    trackingNumber: trackingNumber,
  }).then(() => {
    console.log("Order successfully updated!");
    res.send({message: "Order status changed"});
  }).catch((error) => {
    console.error("Error updating order :" + error.message);
    res.send({message: "Failed to update order"});
  });
});

exports.app = functions.https.onRequest(app);

exports.createStripeCustomer = functions.auth.user().onCreate(async (user) => {
  const customer = await stripe.customers.create({
    email: user.email,
    metadata: {firebaseUID: user.uid},
  });

  await admin.firestore().collection("stripe_customers").doc(user.uid).set({
    customer_id: customer.id,
  });
  return;
});

exports.createEphemeralKey = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError(
        "failed-precondition",
        "The function must be called while authenticated!",
    );
  }
  const uid = context.auth.uid;
  try {
    if (!uid) throw new Error("Not authenticated!");
    // Get stripe customer id
    const customer = (
      await admin.firestore().collection("stripe_customers").doc(uid).get()
    ).data().customer_id;
    const key = await stripe.ephemeralKeys.create(
        {customer: customer},
        {apiVersion: "2022-08-01"},
    );
    return key;
  } catch (error) {
    throw new functions.https.HttpsError("internal", error.message);
  }
});

exports.createStripePayment = functions.firestore
    .document("stripe_customers/{userId}/payments/{pushId}")
    .onCreate(async (snap, context) => {
      const {amount, currency} = snap.data();
      try {
        const customer = (await snap.ref.parent.parent.get())
            .data().customer_id;
        const ephemeralKey = await stripe.ephemeralKeys.create(
            {customer: customer},
            {apiVersion: "2022-08-01"},
        );
        const idempotencyKey = context.params.pushId;
        const payment = await stripe.paymentIntents.create(
            {
              amount,
              currency,
              customer,
            },
            {idempotencyKey},
        );
        payment.ephemeralKey = ephemeralKey.secret;
        await snap.ref.set(payment);
      } catch (error) {
        const errorMesage = error.type ?
            error.message:
            "An error has occurred, please try later.";
        snap.ref.set({error: errorMesage}, {merge: true});
        console.log(errorMesage);
      }
    });

const updatePaymentRecord = async (id) => {
  const payment = await stripe.paymentIntents.retrieve(id);
  const customerId = payment.customer;
  const customersSnap = await admin
      .firestore()
      .collection("stripe_customers")
      .where("customer_id", "==", customerId)
      .get();
  if (customersSnap.size !== 1) throw new Error("User not found!");
  const paymentsSnap = await customersSnap.docs[0].ref
      .collection("payments")
      .where("id", "==", payment.id)
      .get();
  if (paymentsSnap.size !== 1) throw new Error("Payment not found!");
  await paymentsSnap.docs[0].ref.set(payment);
};

exports.handleWebhookEvents = functions.https.onRequest(async (req, resp) => {
  const relevantEvents = new Set([
    "payment_intent.succeeded",
    "payment_intent.processing",
    "payment_intent.payment_failed",
    "payment_intent.canceled",
  ]);

  let event;

  try {
    event = stripe.webhooks.constructEvent(
        req.rawBody,
        req.headers["stripe-signature"],
        functions.config().stripe.webhooksecret,
    );
  } catch (error) {
    console.error("Webhook Error: Invalid Secret");
    resp.status(401).send("Webhook Error: Invalid Secret");
    return;
  }

  if (relevantEvents.has(event.type)) {
    try {
      switch (event.type) {
        case "payment_intent.succeeded":
        case "payment_intent.processing":
        case "payment_intent.payment_failed":
        case "payment_intent.canceled": {
          const id = event.data.object.id;
          await updatePaymentRecord(id);
          break;
        }
        default:
          throw new Error("Unhandled relevant event!");
      }
    } catch (error) {
      console.log("Webhook error for: " + event.data.object.id + ". " +
        error.message,
      );
      resp.status(400).send("Webhook handler failed. View Function logs.");
      return;
    }
  }
  resp.json({received: true});
});

exports.cleanupUser = functions.auth.user().onDelete(async (user) => {
  const dbRef = admin.firestore().collection("stripe_customers");
  const customer = (await dbRef.doc(user.uid).get()).data();
  await stripe.customers.del(customer.customer_id);
  const snapshot = await dbRef
      .doc(user.uid)
      .collection("payment_methods")
      .get();
  snapshot.forEach((snap) => snap.ref.delete());
  await dbRef.doc(user.uid).delete();
  return;
});

/**
 * Add two numbers.
 * @param {string} email The email.
 * @param {string} subject The email subject.
 * @param {string} template The email template.
 * @return {number} The result.
 */
function sendEmail(email, subject, template) {
  firestore.collection("mail")
      .add({
        to: email,
        message: {
          subject: subject,
          text: "",
          html: template,
        },
      }).then(() => console.log("Queued email for delivery!"));
  return 200;
}
