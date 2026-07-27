# GabAI Privacy Policy — DRAFT, NOT YET LEGALLY REVIEWED

**Status: this is a first draft written to seed a real legal review, not a
final policy. Do not publish, link from the app, or represent this as binding
until a lawyer familiar with the Philippine Data Privacy Act (RA 10173) and
its IRR has reviewed it.** Placeholders below are marked `[TBD: ...]`.

This document describes what GabAI collects, why, and what rights you have
over it. GabAI is a mental wellness support app that connects you with an AI
chat assistant and, when needed, human staff and doctors.

## 1. Who we are

`[TBD: legal entity name, business address, contact email/phone]`

This policy covers the GabAI mobile app, the staff/doctor web console, and
the backend services that connect them.

## 2. What we collect

- **Account information**: display name, email address, password (stored
  hashed by our authentication provider, never in plaintext), and the
  province/city you provide during registration.
- **Conversations with GabAI**: every message you send to the chat assistant
  and its replies, along with an automated crisis-risk classification
  (none/moderate/severe) attached to each of your messages.
- **Escalation and support records**: if a conversation is flagged as
  needing human support (by you or automatically), we create a record of
  that request, which staff member handled it, and any notes they add after
  a call.
- **Call metadata**: if you have a voice/video call with staff or a doctor,
  we record when the call started and ended. We do not record or store the
  audio/video content of the call itself — that's handled live by our
  video-calling provider, Daily.co, and is not persisted by GabAI. `[TBD:
  confirm Daily.co's own recording/retention settings match this before
  publishing — if cloud recording is ever enabled, this section needs to
  change]`.

We do **not** collect payment information (GabAI has no in-app purchases at
this stage) or precise device location.

## 3. Why we collect it (purpose and legal basis)

Some of what GabAI collects — conversations about your mental health, crisis
risk level — is "sensitive personal information" under RA 10173, which
requires a stronger legal basis and more careful handling than ordinary
personal data. We process it to:

- Provide the core service: respond to your messages and connect you with
  human support when you ask for it or when our safety systems detect you
  may need it.
- Route you to the right staff member or doctor and give them the context
  they need to help you.
- Improve the safety and quality of our crisis-detection systems.

`[TBD: confirm the specific RA 10173 legal basis relied on for processing
sensitive personal information — likely consent (Sec. 13(a)) combined with
the "protection of life and health" ground (Sec. 13(b)), but this needs
counsel's judgment, not an engineering assumption.]`

## 4. Who can see your information

- **You**, always.
- **GabAI staff and doctor accounts**, but only for patients who have an
  active or past support request — staff cannot browse the conversations of
  patients they have no reason to be helping. `[Note: this scoping was
  implemented and verified at the database level on 2026-07-28 — see the
  engineering notes if counsel wants to confirm the technical mechanism.]`
- We do not sell your information or share it with advertisers.
- **Service providers who process data on our behalf** (see below) —
  none of them are permitted to use your data for their own purposes.

## 5. Third parties who process your data on our behalf

| Provider | What they handle | Why |
|---|---|---|
| Supabase | Account data, conversation records, database hosting | Our database, authentication, and real-time infrastructure |
| Anthropic (Claude) | Message text, for generating replies and crisis classification | Our AI chat provider |
| Google (Gemini) | Message text, currently used as a temporary development/testing stand-in for Anthropic | Same purpose as above, dev-only |
| Daily.co | Live audio/video during a support call | Our video-calling provider |

`[TBD: confirm each provider's own data-processing terms and, where the
provider isn't based in the Philippines, whether a cross-border transfer
mechanism is needed under RA 10173's rules on outsourcing/subcontracting.]`

## 6. How long we keep your information

`[TBD: this is a decision for GabAI's team and counsel to make, not
something this draft can responsibly fill in on its own. Considerations:
clinical/support records may need a minimum retention period for continuity
of care and liability reasons; RA 10173 also expects data to not be kept
longer than necessary for the stated purpose. Once a retention period is
decided, it should be enforced technically, not just written down here.]`

## 7. Your rights

Under RA 10173, you have the right to:

- **Access** the personal data we hold about you.
- **Correct** inaccurate data (update your profile in the app).
- **Object** to processing in certain circumstances.
- **Erasure/blocking** — request that we delete your data. GabAI can
  technically fulfill this request (chat history, escalation records, call
  metadata, and your account are all removed together).
- **Data portability** — request a copy of your data in a portable format.
  `[TBD: not yet technically implemented — currently we can delete on
  request but don't yet have an export feature.]`
- **Lodge a complaint** with the National Privacy Commission if you believe
  your rights have been violated.

To exercise these rights, contact `[TBD: contact channel]`.

**Important limit on erasure**: if you are in the middle of an active crisis
support request, we may need to retain that specific record briefly for
safety and continuity-of-care reasons even if you request deletion —
`[TBD: counsel should define this exception precisely, including how long
"briefly" means and who decides.]`

## 8. Security measures

- Passwords are never stored in plaintext (handled by our authentication
  provider).
- Access to patient data is restricted by account role and, as of
  2026-07-28, scoped to patients a staff/doctor account actually has a
  reason to be helping (see Section 4).
- Data in transit is encrypted (HTTPS/TLS).
- `[TBD: this section should be expanded once a real security policy exists
  — e.g. encryption at rest, access logging/audit trail policy, incident
  response plan, employee/staff confidentiality obligations.]`

## 9. Crisis situations and this policy's limits

GabAI is a supportive companion, not a replacement for professional
diagnosis, therapy, or emergency medical care. If you are in immediate
danger, call 911. See the in-app crisis resources for hotline numbers.

## 10. Changes to this policy

`[TBD: standard "we'll notify you of material changes" language, once this
is a real, published policy.]`

## 11. Contact

`[TBD: Data Protection Officer name/contact, if RA 10173 requires GabAI to
designate one at its current scale — this is a determination for counsel.]`

---

*Engineering note (not part of the policy itself): this draft was written
2026-07-28 to give legal counsel a concrete starting point rather than a
blank page, as part of closing out Phase E's Data Privacy Act punch-list
item. It should not be treated as compliant or complete until reviewed.*
