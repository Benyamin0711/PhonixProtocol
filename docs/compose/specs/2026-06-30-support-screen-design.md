# Support Screen Design Spec

> **Date:** 2026-06-30
> **Status:** Approved

## [S1] Problem

The Support route exists in navigation but no screen is implemented. Users need a way to contact support, view FAQ, rate the app, and connect on social media.

## [S2] Solution Overview

Implement a full Support screen with Contact Form, FAQ, Rate App, and Social Media sections. Follow existing dark theme patterns and bilingual support.

## [S3] Header Section

- Support icon
- "Support Us" title
- Thank you message

## [S4] Contact Form Section

- Name field
- Email field
- Message field
- Send button (placeholder - shows toast)

## [S5] FAQ Section

- Expandable FAQ items:
  - What is Phoenix Protocol?
  - How do boss missions work?
  - How to earn XP?

## [S6] Rate App Section

- Star rating display
- "Rate on Play Store" button (placeholder)

## [S7] Social Media Section

- Instagram link
- Twitter/X link
- GitHub link

## [S8] Architecture

- Create: `SupportScreen.kt` in `feature/support/`
- Modify: `NavGraph.kt` - add Support route composable
- Modify: `DashboardScreen.kt` - wire onNavigateToSupport callback

## [S9] UI Patterns

- BackgroundDark base
- SurfaceDark card containers
- PhoenixOrange for accents and icons
- TextSecondary for labels
- Bilingual strings (EN + FA) for all user-facing text

## [S10] String Resources

New strings needed:
- `support_title`, `support_thank_you`, `support_contact`
- `support_name`, `support_email`, `support_message`, `support_send`
- `support_faq`, `support_faq_1_q`, `support_faq_1_a`, etc.
- `support_rate`, `support_rate_button`, `support_social`
- Persian equivalents for all strings
