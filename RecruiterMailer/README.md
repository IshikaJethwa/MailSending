# Recruiter Mailer (Android)

Save your email profile once (From, subject, body, PDF attachments), then send it
to any recruiter by typing their email and tapping Send. Mail goes out over SMTP
directly from the phone, no backend needed.

## Run
1. Open this folder in Android Studio (Koala or newer) and let Gradle sync.
2. Run on a device or emulator (Android 8.0+).

## Gmail setup (required)
Gmail blocks normal passwords for SMTP. Create an App Password:
1. Turn on 2-Step Verification in your Google Account.
2. Google Account > Security > App passwords > create one for "Recruiter Mailer".
3. Paste the 16-character code into the App password field in the app.

Other providers: change SMTP host/port under "Mail server" (587 = STARTTLS, 465 = SSL).

## Where things live
- data/ProfileStore.kt  - encrypted profile + sent log, PDFs copied to app storage
- mail/MailSender.kt    - SMTP send with attachments (JavaMail for Android)
- ui/SetupScreen.kt     - profile screen
- ui/SendScreen.kt      - To field, Send button, sent history
- res/drawable/ic_launcher_*.xml - adaptive launcher icon
- branding/             - logo SVG + 512px Play Store icon
