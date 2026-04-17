# Tabbar notes

The `tabbar` has `4` possible modes:

- 0 `No tabbar` — only a single page entry, with no tab bar shown at the bottom; typically used for temporary campaign pages.
- 1 `Native tabbar` — switch tabs via `switchTab`; tab pages are cached.
  - Pros: native, renders first, and is cached.
  - Cons: can only use two sets of images to represent the selected / unselected states; changing the color requires replacing the images (or using iconfont).
- 2 `Cached custom tabbar` — switch tabs via `switchTab`; tab pages are cached. Uses the `tabbar` component from a third-party UI library and hides the native tab bar.
  - Pros: freely configure the SVG icons you want, easily change text color, still cached, and supports fancy animations.
  - Cons: the first tap on the tab bar flickers.
- 3 `Uncached custom tabbar` — switch tabs via `navigateTo`; tab pages are NOT cached. Uses the `tabbar` component from a third-party UI library.
  - Pros: freely configure the SVG icons you want, easily change text color, supports fancy animations.
  - Cons: the first tap on the tab bar flickers; no caching.


> Note: any fancy effects must be implemented yourself — this template does not provide them.
