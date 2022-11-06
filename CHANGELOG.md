# AudiTranscribe Changelog

## [0.9.1](https://github.com/AudiTranscribe/AudiTranscribe/compare/v0.9.0...v0.9.1) (2022-11-06)


### Bug Fixes

* update API webserver URL ([fac7ff6](https://github.com/AudiTranscribe/AudiTranscribe/commit/fac7ff60a0617adc2f97cba868c9d3895f94a133))

<hr>

# [0.9.0](https://github.com/AudiTranscribe/AudiTranscribe/compare/v0.8.3...v0.9.0) (2022-11-05)

### Features

* add button to visit app data folder ([e077490](https://github.com/AudiTranscribe/AudiTranscribe/commit/e0774907cff5a76eb670ebc0d47d292516c3dd1b))
* add documentation ([a117b98](https://github.com/AudiTranscribe/AudiTranscribe/commit/a117b9897867349604ef581af51fc5c5e9caf121))
* add Linux support ([8ccceb7](https://github.com/AudiTranscribe/AudiTranscribe/commit/8ccceb7a3426a213df73b6ffc69f58e92d3036fe))
* add more time signature options ([3b933c7](https://github.com/AudiTranscribe/AudiTranscribe/commit/3b933c7f518a4569f1dd24292d6c480c90f04033))
* add undo/redo for note rectangles ([91a92a0](https://github.com/AudiTranscribe/AudiTranscribe/commit/91a92a035a9c74aa9c1912619c93e7ba48603349))
* be more generous with exceptions ([b13c633](https://github.com/AudiTranscribe/AudiTranscribe/commit/b13c633734af25913d324751a71c2aac76107f16))
* increase time signature options ([a50c4f6](https://github.com/AudiTranscribe/AudiTranscribe/commit/a50c4f6cdaf541c8bb40f193f7e97d56b97f81ed))


### Bug Fixes

* cancelling saving project shows progress bar ([11c167d](https://github.com/AudiTranscribe/AudiTranscribe/commit/11c167de62ab0afc3af86825840a69fb39c997c2)), closes [#28](https://github.com/AudiTranscribe/AudiTranscribe/issues/28)
* fix an imprecision in note label alignment ([d15a9e6](https://github.com/AudiTranscribe/AudiTranscribe/commit/d15a9e6816659a5508ecd74bc6470b0cc101f90b)), closes [#22](https://github.com/AudiTranscribe/AudiTranscribe/issues/22)
* fix issue where preferences view could be hidden ([922e025](https://github.com/AudiTranscribe/AudiTranscribe/commit/922e02504a3a0c148c722ce3127b2fba560264c2))
* fix issue with current octave rectangle leaving screen ([0df6ca9](https://github.com/AudiTranscribe/AudiTranscribe/commit/0df6ca9d58c0ea593ec68920aa0877a6e63bdd94))
* fix weird resizing of buttons ([66acdcc](https://github.com/AudiTranscribe/AudiTranscribe/commit/66acdccd5fd25931d557eb7e031dbfe1ce6eb2fd))
* fix weird resizing of hyperlinks upon hover ([c673a5a](https://github.com/AudiTranscribe/AudiTranscribe/commit/c673a5adef003f3905b49e85adeb2d354d57334b))
* incorrect positioning of transcription window ([946604d](https://github.com/AudiTranscribe/AudiTranscribe/commit/946604d1d7f85e9a11564021416c042378ff9d69))
* weird window overflow was fixed ([22270df](https://github.com/AudiTranscribe/AudiTranscribe/commit/22270df7117d541cd4a84637f0faf2cfaf4b78b9))


### Performance Improvements

* change divides to multiplies ([30feb60](https://github.com/AudiTranscribe/AudiTranscribe/commit/30feb6047bac939134ff4c13d63a1fc6b4326adb))
* change seconds per beat calculation ([c90ab8c](https://github.com/AudiTranscribe/AudiTranscribe/commit/c90ab8ca41e92767e7ec20771c6a37fbf2385c3c))
* clean up code in some files ([f7e608e](https://github.com/AudiTranscribe/AudiTranscribe/commit/f7e608e1da33e81939fdcdbcc0359759b7f6efc2))
* improve performance in `BPMEstimationHelpers` ([4e645f5](https://github.com/AudiTranscribe/AudiTranscribe/commit/4e645f54295f7bb65d350960fe6f41d690af0d26))
* improve performance in `PlottingStuffHandler` ([a48228b](https://github.com/AudiTranscribe/AudiTranscribe/commit/a48228b25588000b7459b98f8ac4a7d7fb361d90))
* improve performance in `Wavelet` ([f5190b9](https://github.com/AudiTranscribe/AudiTranscribe/commit/f5190b9f4dc3869d8d38ce9a4924c25133509d5f))
* improve performance of `ArrayUtils` ([22dfabd](https://github.com/AudiTranscribe/AudiTranscribe/commit/22dfabd39dff47157e4a2d9cbc8de59b2e34dce5))
* improve performance of `Complex` ([402ff1e](https://github.com/AudiTranscribe/AudiTranscribe/commit/402ff1e3997c511b603a3ef69926b182d5baa384))
* improve performance of `FFT` ([3b864ee](https://github.com/AudiTranscribe/AudiTranscribe/commit/3b864ee91156a3a23318258a534574d14795c06d))
* improve performance of `FrequencyBins` ([69c642c](https://github.com/AudiTranscribe/AudiTranscribe/commit/69c642ca6ca3342db45034a2709d6151f46d1c35))
* improve performance of `UnitConversionUtils` ([3321d5a](https://github.com/AudiTranscribe/AudiTranscribe/commit/3321d5a3780ae73f3683ca326bfe5803e965e4a4))
* update use of ceiling/floor divisions ([cfe7331](https://github.com/AudiTranscribe/AudiTranscribe/commit/cfe7331d65125c901a7e39893d42db0be966a446))

# [0.8.3](https://github.com/AudiTranscribe/AudiTranscribe/compare/v0.8.2...v0.8.3) (2022-10-23)


### Bug Fixes

* fix incorrectly sized button on the project setup view ([fc6ca07](https://github.com/AudiTranscribe/AudiTranscribe/commit/fc6ca07f743684508658f9c03df209c7b07664e9))

## Before 0.8.3

Note that a changelog was not maintained before version 0.8.3.
