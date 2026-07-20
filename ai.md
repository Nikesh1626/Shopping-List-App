# Shopping List App Feature Inventory

This file summarizes the features currently present in the app and separates them from features that are hinted at in the UI but are not implemented yet.

## Implemented Features

- Single-screen shopping list app built with Jetpack Compose.
- Displays a shopping list header with a live product count based on the current number of items.
- Floating action button to add a new shopping item.
- Add item dialog with fields for item name, quantity, and category.
- Edit item dialog that opens with the selected item's existing values prefilled.
- Delete action for removing an item from the list.
- Search field that filters items by item name or category.
- Category pills that can set the search query when tapped.
- Category pills are built from a mix of default categories and categories already used in existing items.
- Shopping item cards show the item initial, item name, quantity, and category.
- Optional quantity support; invalid or empty quantity input is stored as no quantity.
- Blank categories are supported and shown as "Uncategorized" in the item card.
- Cancel action for dismissing the add/edit dialog without saving.
- Custom themed UI with search card, category grid, floating action button, and bottom navigation styling.

## Not Yet Implemented

- Real bottom navigation behavior. The `Lists`, `Search`, and `Profile` tabs only change the selected tab state and do not switch screens or content.
- Multiple shopping lists. The app currently manages only one in-memory list even though the bottom bar suggests a `Lists` section.
- Profile functionality. A `Profile` tab exists in the UI, but there is no profile screen, account data, or related logic.
- Dedicated search screen. Search works inside the main screen, but there is no separate screen behind the `Search` bottom tab.
- `See all` category action. The label is shown in the UI, but it has no click behavior and does not open a full category view.
- Persistent storage. Items are kept only in ViewModel memory, so the list does not survive app restarts or process death.
- Empty-state UX. When there are no items, the app does not show a dedicated empty-state message or onboarding prompt.
- Advanced list management such as sorting, completion tracking, favorites, or category management.
