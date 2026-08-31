# Screen map (iOS ↔ Android)

Every user-facing surface. Use this to jump between an iOS reference file
and its Android equivalent. Test tags are the canonical strings — mirror the
iOS `accessibilityIdentifier` value.

Paths are relative to their respective repo roots:
- iOS: `/Volumes/Ewynman/Dev/spot-ios-app/`
- Android: `/Volumes/Ewynman/Dev/spot-android-app/`

## App shell & navigation

| Surface | iOS | Android | Test tag |
|---------|-----|---------|----------|
| Entry point | `Spot/SpotApp.swift` | `MainActivity.kt` + `SpotApplication.kt` | — |
| Root gate | `Spot/Views/RootView.swift` | `feature/launch/SpotAppRoot.kt` + `LaunchGateResolver.kt` | — |
| Tab shell | `Spot/Views/MainTabView.swift` + `Components/BottomTabNavigationView.swift` | `navigation/SpotNavHost.kt` + `SpotBottomBar.kt` | `main.tabShell` |
| Home tab | `HomepageView.swift` | `feature/home/HomeScreen.kt` | `navigation.homeTab` |
| Map tab | `Home/MapView.swift` | `feature/map/MapScreen.kt` | `navigation.mapTab` |
| Post tab | `PostFlow/PostFlowView.swift` (via `PostTabView`) | `feature/post/PostScreen.kt` | `navigation.postTab` |
| Search tab | `Search/SearchView.swift` | `feature/search/SearchScreen.kt` | `navigation.searchTab` |
| Profile tab | `Profile/ProfileView.swift` | `feature/profile/ProfileScreen.kt` | `navigation.profileTab` |

## Launch / auth

| Surface | iOS | Android | Test tag |
|---------|-----|---------|----------|
| Splash | `Views/Launch/LaunchView.swift` | `feature/launch/LaunchSplashScreen.kt` | `launch.splash`, `launch.splash.wordmark` |
| Welcome (unauth) | `Views/Onboarding/WelcomeView.swift` | `feature/auth/WelcomeScreen` | `welcome.screen`, `onboarding.getStartedButton` |
| Welcome back | `Views/Auth/WelcomeBackView.swift` | (integrated in `WelcomeScreen`) | `auth.welcomeBack.*` |
| Login | `Views/Auth/LoginView.swift` | `feature/auth/LoginScreen` | `auth.login.screen`, `auth.login.emailField`, `auth.login.passwordField`, `auth.login.submitButton` |
| Sign up | `Views/Auth/SignupView.swift` | `feature/auth/SignUpScreen` | `onboarding.signupScreen`, `auth.signup.*` |
| Email OTP | `Views/Auth/ConfirmEmailView.swift` | `feature/auth/ConfirmEmailScreen` | `auth.confirmEmail.*`, `confirmEmail.*` |
| Apple / Google button | `Views/Auth/ThemedAppleSignInButton.swift` | `feature/auth/component/*` (Google) | `auth.signInWithAppleButton` → `auth.signInWithGoogleButton` |
| Terms checkbox | `Views/Components/TermsAgreementCheckboxView.swift` | `feature/auth/component/TermsAgreementCheckbox` | `auth.termsCheckbox`, `auth.termsAgreementText` |
| Post-auth username | `Views/Onboarding/PostAuthSetupFlowView.swift` | `feature/onboarding/UsernameSetupScreen` | `usernameSetup.*`, `postAuth.continueButton` |
| Terms update gate | `Views/Onboarding/TermsUpdateGateView.swift` | `feature/onboarding/TermsUpdateScreen` | `auth.termsUpdateGate`, `auth.acceptUpdatedTermsButton` |

## Onboarding / first-run coach

| Surface | iOS | Android | Test tag |
|---------|-----|---------|----------|
| Coach overlay | `Views/Components/CoachMarkOverlay.swift` + `Managers/HomeTourManager.swift` (`SpotFirstRunOnboardingManager`) | `feature/onboarding/SpotFirstRunOnboardingOverlay.kt` + `FirstRunOnboardingHost.kt` | `onboarding.coachOverlay`, `onboarding.coachTitle/Body/Primary/Skip/Back/Progress` |

Coach steps (13): welcome → spot card → vibe tag → like → bookmark → flip →
creator → map tab → user location → markers → preview → finale.

## Home feed

| Surface | iOS | Android | Test tag |
|---------|-----|---------|----------|
| Home shell | `Views/Home/HomepageView.swift` + `FeedViewModel.swift` | `feature/home/HomeScreen.kt` + `HomeFeedViewModel.kt` | `home.feedRoot` |
| Feed list | `Components/FeedContentView.swift` | `feature/home/HomeScreen.kt` (inline) | `home.feedList` |
| Spot card | `Components/SpotCard.swift` | `core/design/component/SpotCard.kt` | `home.spotCard`, `spot.card`, `home.spotCard.like`, `home.spotCard.flip`, `home.spotCard.openInMap` |
| Skeleton | `Components/SkeletonSpotCard.swift` | `core/design/component/SkeletonSpotCard.kt` | `home.feedLoading` |
| Empty | `Components/EmptyFeedView.swift` | `core/design/component/EmptyFeedView.kt` | `home.feedEmpty.*` |
| Map preview flip | `Components/HomeSpotMapPreview.swift` | (in `SpotCard.kt`) | `home.spotCard.flip` |
| Publish banner | `Components/BottomTabNavigationView.PublishBannerView` | Home layer in `HomeScreen.kt` | `home.publishBanner` |

## Map

| Surface | iOS | Android | Test tag |
|---------|-----|---------|----------|
| Map root | `Views/Home/MapView.swift` + `MapViewModel.swift` | `feature/map/MapScreen.kt` + `MapViewModel.kt` | `map.mapRoot`, `map.googleMap`, `map.screen` |
| Filter controls | `Views/Components/Map/MapControlsOverlay.swift` + `MapFilterControls.swift` | `feature/map/MapFilterPillsRow.kt` + `MapVibeFilterSheet.kt` | `map.filterPills`, `map.filter.*`, `map.filterButton` |
| Preview card | `Views/Components/Map/MapSpotPreviewCard.swift` | `feature/map/MapSpotDrawer.kt` (compact) | `map.spotPreview`, `map.preview.save`, `map.spotPreviewClose` |
| Detail sheet | `Views/Components/Map/SpotDetailSheet.swift` | `feature/map/MapSpotDrawer.kt` (expanded) | `map.spotDetail`, `map.detail.save`, `map.drawerExpandToggle` |
| Recenter | `MapControlsOverlay.swift` | `feature/map/MapScreen.kt` | `map.recenter`, `map.recenterButton` |
| Markers | `Components/Map/SpotAnnotationView.swift`, `SpotMarkerView.swift` | `feature/map/MapPinMarker.kt` | `mapPin.*` |
| User location | `Components/Map/UserLocationAnnotationView.swift` | `feature/map/MapUserLocationMarker.kt` | — |
| Profile map tab | `Views/Profile/ProfileMapView.swift` | `feature/map/ProfileMapView.kt` | (embedded in profile) |

## Post composer

| Surface | iOS | Android | Test tag |
|---------|-----|---------|----------|
| Flow shell | `Views/PostFlow/PostFlowView.swift` + `PostFlowViewModel.swift` | `feature/post/PostScreen.kt` + `PostViewModel.kt` | `post.postRoot`, `post.composer` |
| Step 1 (photos) | `PhotoSelectionView.swift`, `SpotPhotoEditorView.swift` | `feature/post/PostComposerSteps.kt` (photos section) | `post.step.photos`, `posting.photoStepRoot`, `posting.choosePhotosButton`, `post.takePhoto`, `post.chooseGallery` |
| Step 2 (location) | `LocationSelectionView.swift` | Same, location section | `post.step.location`, `locationSearchField`, `nearbyPlacesSection` |
| Step 3 (vibes) | `VibeSelectionView.swift`, `VibePhotoMappingSection.swift` | Same, vibes section | `post.step.vibes`, `vibePhotoMappingSection` |
| Rules sheet | `Views/PostFlow/PostingRulesView.swift` | `feature/post/PostSheets.kt` | `post.postingRulesSheet` |
| Drafts sheet | (inline) | `feature/post/PostSheets.kt` | `post.draftsSheet`, `posting.draftsButton` |
| Publish button | in `PostFlowView` | in `PostScreen` | `post.publishButton` |
| Edit spot (Pro) | `Views/PostFlow/EditSpotView.swift` + `EditSpotViewModel.swift` | **MISSING — task 01** | `editSpot.*` |

## Search

| Surface | iOS | Android | Test tag |
|---------|-----|---------|----------|
| Search root | `Views/Search/SearchView.swift` + `SearchViewModel.swift` | `feature/search/SearchScreen.kt` + `SearchViewModel.kt` | `search.searchRoot` |
| Segment control | (inline) | `feature/search/SearchComponents.kt` | `search.segmentControl`, `search.segment.*` |
| Query field | (inline) | Same | `search.queryField` |
| History | via `SearchHistoryManager.swift` | `feature/search/SearchComponents.kt` + `SearchHistoryStore.kt` | `search.history.*` |
| User result | (inline row) | Same | `search.user.*` |
| Location result | (inline row) | Same | `search.location.*` |
| Vibe result | (inline row) | Same | `search.vibe.*` |
| Grid | (inline) | Same | `search.gridRoot`, `search.spotGrid` |
| Expanded spot | (inline) | Same | `search.expandedSpot` |
| User profile push | via `NavigationStack` | Same | `search.userProfile` |
| Bookmark on grid | (inline, Pro-aware) | **NEEDS COLLECTION PICKER — task 02** | — |

## Profile & social

| Surface | iOS | Android | Test tag |
|---------|-----|---------|----------|
| Profile root | `Views/Profile/ProfileView.swift` + `ProfileViewModel.swift` | `feature/profile/ProfileScreen.kt` + `ProfileViewModel.kt` | `profile.profileRoot`, `profile.screenRoot` |
| Header | (inline) | `feature/profile/ProfileComponents.kt` | `profile.header`, `profile.avatar` |
| Spots tab | `SpotGridScreen.swift` | Same | `profile.spotGrid`, `profile.tab.spots`, `profile.spotsTab` |
| Map tab | `ProfileMapView.swift` | Same | `profile.tab.map`, `profile.mapTab` |
| Follow / unfollow | (inline) | Same | `profile.followButton`, `profile.unfollowButton` |
| Overflow menu | (inline) | `feature/profile/ProfileComponents.kt` | `profile.overflow*`, `profile.menu.*` |
| Report / block | via `ModerationService` | `feature/safety/SafetyFlowHost.kt` | `profile.reportUserAction`, `profile.blockUserAction` |
| Settings entry | (inline) | `feature/profile/ProfileScreen.kt` | `profile.settingsEntry` |
| Loading | `ProfileLoadingPlaceholder.swift` | inline skeleton | — |
| Empty (own / other / private) | `ProfileSpotsEmptyState` | inline | `profile.spotsEmptyState`, `profile.postFirstSpotButton` |
| Follow requests | `Views/Profile/FollowRequestsView.swift` | `feature/profile/FollowRequestsScreen.kt` | `profile.followRequests*` |
| Algorithm debug | `AlgorithmDebugView.swift` + `FeedProfileViewModel.swift` | `feature/settings/AlgorithmSnapshotScreen.kt` + `AlgorithmSnapshotViewModel.kt` | (task 05 will add tags) |
| Bookmark on profile grid | (inline, Pro-aware) | **NEEDS COLLECTION PICKER — task 02** | — |

## Collections (Pro)

| Surface | iOS | Android | Test tag |
|---------|-----|---------|----------|
| Collections list (in Settings) | `Views/Profile/CollectionsView.swift` | `feature/collections/CollectionsListScreen.kt` | `collections.listRoot` |
| Collection detail | `Views/Profile/BookmarksCollectionsScreen.swift` | `feature/collections/CollectionDetailScreen.kt` | `collection.detailRoot` |
| Picker sheet | `CollectionManagerSheet.swift` | `feature/collections/CollectionPickerSheet.kt` | `collections.pickerSheet` |
| Card | `CollectionCardView.swift` | (inline in list) | `collections.item.*` |

## Settings

| Surface | iOS | Android | Test tag |
|---------|-----|---------|----------|
| Settings root | `Views/Profile/SettingsView.swift` | `feature/settings/SettingsScreen.kt` + `SettingsNavigationHost.kt` | `settings.root`, `settings.screenRoot` |
| Account | (row group) | `feature/settings/AccountSettingsScreen.kt` | `settings.account`, `settings.accountSettingsEntry` |
| Security | (row group) | `feature/settings/SecuritySettingsScreen.kt` | `settings.security` |
| Subscription | (row group) | `feature/settings/SubscriptionSettingsScreen.kt` | `settings.subscription` |
| Permissions | `Views/Profile/PermissionsSettingsView.swift` | `feature/settings/PermissionsSettingsScreen.kt` | `settings.permissions`, `settings.permissionsRow`, `permissions.*` |
| Legal | (inline) | `feature/settings/LegalScreen.kt` | `settings.legal` |
| Debug logging | `Views/Profile/LoggingSettingsDetailView.swift` | `feature/settings/DebugLoggingScreen.kt` | `settings.debug.logging` |
| Algorithm snapshot | `AlgorithmDebugView.swift` | `feature/settings/AlgorithmSnapshotScreen.kt` | `settings.debug.algorithm` |
| Blocked users | `Views/Settings/BlockedUsersView.swift` | `feature/settings/SecuritySettingsScreen.kt` (embedded) | (verify) |
| Delete account | (inline) | `feature/settings/AccountSettingsScreen.kt` | `settings.deleteAccount*` |
| Collections entry | (row) | `feature/settings/SettingsNavigationHost.kt` **TODO — task 08** | — |

## Pro / paywall

| Surface | iOS | Android | Test tag |
|---------|-----|---------|----------|
| Paywall sheet | `Views/PaywallView.swift` | `feature/billing/PaywallScreen.kt` + `overlay/PaywallSheet` | `paywall.*`, `overlay.paywall` |
| Pro success | `Views/ProSuccessView.swift` | `feature/billing/ProSuccessScreen.kt` | `proSuccess.*` |
| Post-purchase onboarding | `PostPurchaseProOnboardingView.swift` + `PostPurchaseProOnboardingManager.swift` | `feature/billing/ProOnboardingTour.kt` | `proOnboarding.*` |

Paywall triggers: Settings "Go Pro", post flow multi-vibe upsell, Edit spot
(Pro-only), Profile Pro upsell entry, free bookmark cap reached.

## Safety

| Surface | iOS | Android | Test tag |
|---------|-----|---------|----------|
| Spot overflow menu | `SpotCard.swift` inline | `feature/safety/SafetyFlowHost.kt` | `safety.spotOverflowMenu` |
| Profile overflow menu | `ProfileView.swift` inline | Same | `safety.profileOverflowMenu` |
| Report sheet (spot) | `Views/ReportSheet.swift` | `feature/safety/ReportSheet.kt` | `safety.reportSheet.*` |
| Report sheet (profile) | `Views/ProfileReportSheet.swift` | Same (dual-mode) | `profileReport.reason.*`, `profileReport.submitButton` |
| Block dialog | (inline confirmation) | `feature/safety/BlockUserDialog.kt` | `safety.blockDialog.*` |
| Block confirmation | `Components/SpotConfirmationOverlay.swift` | Toast + optimistic remove | `safety.successToast` |

## Deep link overlays

| Surface | iOS | Android | Test tag |
|---------|-----|---------|----------|
| Spot loading | `RootView.swift` overlay | `feature/overlay/SpotLoadingOverlay.kt` | `overlay.spotLoading` |
| Spot detail | `SpotCard.swift` (`.detail` presentation) | `feature/overlay/SpotDetailOverlay.kt` | `overlay.spotDetail` |
| Spot unavailable | `Views/SpotUnavailableView.swift` | `feature/overlay/SpotUnavailableOverlay.kt` | `overlay.spotUnavailable` |

## Notifications

| Surface | iOS | Android | Test tag |
|---------|-----|---------|----------|
| Permission pre-prompt | `Views/NotificationPermissionView.swift` | `core/design/component/PermissionPrePrompt.kt` (`notifications` variant) | `permissionPrePrompt.*` |
| Local notification (accepted) | `Services/Notifications/NotificationService.swift` | `data/notifications/SpotNotificationService.kt` (`FOLLOW_ACCEPTED` channel) | — |
| Remote push (received) | `Services/Notifications/NotificationService.swift` (local only on iOS) | **MISSING — task 03 adds FCM** | — |

## Legend

- **Test tag** matches the iOS `accessibilityIdentifier` string. Compose uses
  `Modifier.testTag(...)`; the same string works for shared XCTest / Espresso
  vocabulary.
- **MISSING / TODO** is a documented gap — see the linked task file.
