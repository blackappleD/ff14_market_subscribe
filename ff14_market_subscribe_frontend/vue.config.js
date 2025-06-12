const { defineConfig } = require('@vue/cli-service');
const path = require('path');

module.exports = defineConfig({
  transpileDependencies: true,
  publicPath: process.env.NODE_ENV === 'production' ? '/ff14_market/' : '/',
  outputDir: 'dist',
  assetsDir: 'static',
  productionSourceMap: false,
  chainWebpack: config => {
    config.plugin('html').tap(args => {
      args[0].title = '最终幻想14实时物价订阅';
      return args;
    });
  },
  devServer: {
    port: 13000,
    historyApiFallback: {
      rewrites: [
        { from: /./, to: '/index.html' }
      ]
    },
    proxy: {
      '/ff14': {
        target: 'http://localhost:18888',
        changeOrigin: true,
        ws: true
      }
    }
  }
});