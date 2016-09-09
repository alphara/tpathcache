var path = require('path');
var webpack = require('webpack');

module.exports = {
  entry: './react.js',
  output: {
    path: __dirname + '/resources/public/build',
    publicPath: '/build/',
    filename: 'bundle.js' },
  module: {
    loaders: [
      {
        test: /.jsx?$/,
        loader: 'babel-loader',
        exclude: /node_modules/,
        query: {
          presets: ['es2015', 'react', 'stage-0']
        }
      }
    ]
  },
};

