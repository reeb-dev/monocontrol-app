# Google Play — App access instructions (copy-paste)

Use this file when Play Console asks for **instructions to access restricted parts** of the app. **Replace every `YOUR_*` placeholder** with your real demo account before submitting.

All review-facing text below is in **English**, as required by Google Play.

---

## 1. Instruction name *(max 60 characters)*

Pick one:

```
Instructions for MonoControl reviewer login
```
*(47 characters)*

Alternative:

```
MonoControl Play review demo access
```
*(35 characters)*

---

## 2. Username / email / phone *(max 100 characters)*

Create a **dedicated** test account (recommended: Gmail used only for review). Example format once you replace it:

```
reviewer.monocontrol.YOURPROJECT@gmail.com
```

Or paste only the exact email you registered in Firebase Authentication for this demo user.

**Do not** use your personal production account if it holds real client data.

---

## 3. Password

Use a **strong unique password** only for this demo account. Enter the same password in Play Console **Password** field.

Example placeholder (replace before saving):

```
YOUR_DEMO_PASSWORD_HERE
```

---

## 4. Additional instructions *(free-text / “Notes”, if the form has it)*

Paste this block and adjust steps if your UI strings differ slightly:

```
SIGN-IN METHOD
- Use Email / Password login only (not Google) so reviewers can access without configuring OAuth.

STEPS
1. Install and open the app.
2. On the login screen, enter the demo email and password provided above.
3. If onboarding appears (name/CUIT), fill any valid-format sample data (e.g. name "Demo User", CUIT in Argentine format) and continue.
4. After login, the main bottom navigation is visible: Home, Movements (Ingresos), Category, Profile. For Contador-only tabs (Clients, Collections), open Profile and set the role to Contador if such an option exists, then return to the bottom nav.

RESTRICTED AREAS
- All features after authentication require the demo account. There is no separate paywall for core flows.

NOTES
- Push notifications may be denied; core flows work without them.
- If the demo account stops working, we will update these instructions in the next submission.
```

---

## 5. Before each release — checklist

- [ ] Demo account still exists in **Firebase Authentication** (email/password enabled).
- [ ] Password in Play Console matches the demo account.
- [ ] No **2FA** or extra verification on the demo account (or document how to complete it).
- [ ] If you changed login flow, update section 4 text in Play Console.

---

## Firebase setup reminder

In Firebase Console → **Authentication** → **Sign-in method**: ensure **Email/Password** is enabled so Google reviewers can sign in with the credentials you supply.
