module.exports = {
  webpack: {
    configure: {
      // react-datepicker v9 uses a dynamic require() to optionally load date-fns-tz.
      // Webpack cannot statically analyse this and emits a "Critical dependency" warning,
      // which CI (CI=true) would escalate into a build failure.
      ignoreWarnings: [/Critical dependency/],
    },
  },
};
