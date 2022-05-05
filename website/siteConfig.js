const apiUrl = "/Proptics/api/proptics/index.html";

const siteConfig = {
  title: 'Proptics[_, _]',
  tagline: 'Profunctor optics and lenses library for Scala',
  url: 'https://sagifogel.github.io/Proptics',
  baseUrl: '/Proptics/',
  apiUrl,
  favicon: 'img/favicon/favicon.ico',
  logo: '/proptics-transparent.png',
  repoUrl: 'https://github.com/sagifogel/proptics',
  customDocsPath: "docs/target/mdoc",
  projectName: 'Proptics',
  organizationName: 'sagifogel',
  docsSideNavCollapsible: true,
  headerLinks: [
    {href: '/Proptics/docs/overview', label: 'Getting Started'},
    {href: apiUrl, label: "API Docs"},
    {href: 'https://github.com/sagifogel/proptics', label: 'GitHub'}
  ],
  colors: {
    primaryColor: '#B9459A',
    secondaryColor: '#8A3373',
  },
  copyright: `Copyright © ${new Date().getFullYear()} Sagi Fogel (foldl)`,
  usePrism: ['scala'],
  highlight: {
    theme: 'atom-one-dark',
  },
  separateCss: ["api"],
  scripts: ['https://buttons.github.io/buttons.js'],
  onPageNav: 'separate',
  cleanUrl: true
};

module.exports = siteConfig;