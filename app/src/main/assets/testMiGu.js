var md5;
!function(n) {
    "use strict";
    function d(n, t) {
        var r = (65535 & n) + (65535 & t);
        return (n >> 16) + (t >> 16) + (r >> 16) << 16 | 65535 & r
    }
    function f(n, t, r, e, o, u) {
        return d((u = d(d(t, n), d(e, u))) << o | u >>> 32 - o, r)
    }
    function l(n, t, r, e, o, u, c) {
        return f(t & r | ~t & e, n, t, o, u, c)
    }
    function g(n, t, r, e, o, u, c) {
        return f(t & e | r & ~e, n, t, o, u, c)
    }
    function v(n, t, r, e, o, u, c) {
        return f(t ^ r ^ e, n, t, o, u, c)
    }
    function m(n, t, r, e, o, u, c) {
        return f(r ^ (t | ~e), n, t, o, u, c)
    }
    function c(n, t) {
        var r, e, o, u;
        n[t >> 5] |= 128 << t % 32,
        n[14 + (t + 64 >>> 9 << 4)] = t;
        for (var c = 1732584193, f = -271733879, i = -1732584194, a = 271733878, h = 0; h < n.length; h += 16)
            c = l(r = c, e = f, o = i, u = a, n[h], 7, -680876936),
            a = l(a, c, f, i, n[h + 1], 12, -389564586),
            i = l(i, a, c, f, n[h + 2], 17, 606105819),
            f = l(f, i, a, c, n[h + 3], 22, -1044525330),
            c = l(c, f, i, a, n[h + 4], 7, -176418897),
            a = l(a, c, f, i, n[h + 5], 12, 1200080426),
            i = l(i, a, c, f, n[h + 6], 17, -1473231341),
            f = l(f, i, a, c, n[h + 7], 22, -45705983),
            c = l(c, f, i, a, n[h + 8], 7, 1770035416),
            a = l(a, c, f, i, n[h + 9], 12, -1958414417),
            i = l(i, a, c, f, n[h + 10], 17, -42063),
            f = l(f, i, a, c, n[h + 11], 22, -1990404162),
            c = l(c, f, i, a, n[h + 12], 7, 1804603682),
            a = l(a, c, f, i, n[h + 13], 12, -40341101),
            i = l(i, a, c, f, n[h + 14], 17, -1502002290),
            c = g(c, f = l(f, i, a, c, n[h + 15], 22, 1236535329), i, a, n[h + 1], 5, -165796510),
            a = g(a, c, f, i, n[h + 6], 9, -1069501632),
            i = g(i, a, c, f, n[h + 11], 14, 643717713),
            f = g(f, i, a, c, n[h], 20, -373897302),
            c = g(c, f, i, a, n[h + 5], 5, -701558691),
            a = g(a, c, f, i, n[h + 10], 9, 38016083),
            i = g(i, a, c, f, n[h + 15], 14, -660478335),
            f = g(f, i, a, c, n[h + 4], 20, -405537848),
            c = g(c, f, i, a, n[h + 9], 5, 568446438),
            a = g(a, c, f, i, n[h + 14], 9, -1019803690),
            i = g(i, a, c, f, n[h + 3], 14, -187363961),
            f = g(f, i, a, c, n[h + 8], 20, 1163531501),
            c = g(c, f, i, a, n[h + 13], 5, -1444681467),
            a = g(a, c, f, i, n[h + 2], 9, -51403784),
            i = g(i, a, c, f, n[h + 7], 14, 1735328473),
            c = v(c, f = g(f, i, a, c, n[h + 12], 20, -1926607734), i, a, n[h + 5], 4, -378558),
            a = v(a, c, f, i, n[h + 8], 11, -2022574463),
            i = v(i, a, c, f, n[h + 11], 16, 1839030562),
            f = v(f, i, a, c, n[h + 14], 23, -35309556),
            c = v(c, f, i, a, n[h + 1], 4, -1530992060),
            a = v(a, c, f, i, n[h + 4], 11, 1272893353),
            i = v(i, a, c, f, n[h + 7], 16, -155497632),
            f = v(f, i, a, c, n[h + 10], 23, -1094730640),
            c = v(c, f, i, a, n[h + 13], 4, 681279174),
            a = v(a, c, f, i, n[h], 11, -358537222),
            i = v(i, a, c, f, n[h + 3], 16, -722521979),
            f = v(f, i, a, c, n[h + 6], 23, 76029189),
            c = v(c, f, i, a, n[h + 9], 4, -640364487),
            a = v(a, c, f, i, n[h + 12], 11, -421815835),
            i = v(i, a, c, f, n[h + 15], 16, 530742520),
            c = m(c, f = v(f, i, a, c, n[h + 2], 23, -995338651), i, a, n[h], 6, -198630844),
            a = m(a, c, f, i, n[h + 7], 10, 1126891415),
            i = m(i, a, c, f, n[h + 14], 15, -1416354905),
            f = m(f, i, a, c, n[h + 5], 21, -57434055),
            c = m(c, f, i, a, n[h + 12], 6, 1700485571),
            a = m(a, c, f, i, n[h + 3], 10, -1894986606),
            i = m(i, a, c, f, n[h + 10], 15, -1051523),
            f = m(f, i, a, c, n[h + 1], 21, -2054922799),
            c = m(c, f, i, a, n[h + 8], 6, 1873313359),
            a = m(a, c, f, i, n[h + 15], 10, -30611744),
            i = m(i, a, c, f, n[h + 6], 15, -1560198380),
            f = m(f, i, a, c, n[h + 13], 21, 1309151649),
            c = m(c, f, i, a, n[h + 4], 6, -145523070),
            a = m(a, c, f, i, n[h + 11], 10, -1120210379),
            i = m(i, a, c, f, n[h + 2], 15, 718787259),
            f = m(f, i, a, c, n[h + 9], 21, -343485551),
            c = d(c, r),
            f = d(f, e),
            i = d(i, o),
            a = d(a, u);
        return [c, f, i, a]
    }
    function i(n) {
        for (var t = "", r = 32 * n.length, e = 0; e < r; e += 8)
            t += String.fromCharCode(n[e >> 5] >>> e % 32 & 255);
        return t
    }
    function a(n) {
        var t = [];
        for (t[(n.length >> 2) - 1] = void 0,
        e = 0; e < t.length; e += 1)
            t[e] = 0;
        for (var r = 8 * n.length, e = 0; e < r; e += 8)
            t[e >> 5] |= (255 & n.charCodeAt(e / 8)) << e % 32;
        return t
    }
    function e(n) {
        for (var t, r = "0123456789abcdef", e = "", o = 0; o < n.length; o += 1)
            t = n.charCodeAt(o),
            e += r.charAt(t >>> 4 & 15) + r.charAt(15 & t);
        return e
    }
    function r(n) {
        return unescape(encodeURIComponent(n))
    }
    function o(n) {
        return i(c(a(n = r(n)), 8 * n.length))
    }
    function u(n, t) {
        return function(n, t) {
            var r, e = a(n), o = [], u = [];
            for (o[15] = u[15] = void 0,
            16 < e.length && (e = c(e, 8 * n.length)),
            r = 0; r < 16; r += 1)
                o[r] = 909522486 ^ e[r],
                u[r] = 1549556828 ^ e[r];
            return t = c(o.concat(a(t)), 512 + 8 * t.length),
            i(c(u.concat(t), 640))
        }(r(n), r(t))
    }
    function t(n, t, r) {
        return t ? r ? u(t, n) : e(u(t, n)) : r ? o(n) : e(o(n))
    }
    md5=t;
    "function" == typeof define && define.amd ? define(function() {
        return t
    }) : "object" == typeof module && module.exports ? module.exports = t : n.md5 = t
}(this);

const sb = '<G6sX,Lk~^2:Y%4Z';

function encode(plainText) {
  const now = new Date().getTime();
  const md5Data = md5(sb);
  let left = md5(md5Data.substr(0, 16));
  let right = md5(md5Data.substr(16, 32));
  let nowMD5 = md5(now).substr(-4);
  let Var_10 = (left + md5((left + nowMD5)));
  let Var_11 = Var_10['length'];
  let Var_12 = ((((now / 1000 + 86400) >> 0) + md5((plainText + right)).substr(0, 16)) + plainText);
  let Var_13 = '';
  for (let i = 0, Var_15 = Var_12.length;
    (i < Var_15); i++) {
    let Var_16 = Var_12.charCodeAt(i);
    if ((Var_16 < 128)) {
      Var_13 += String['fromCharCode'](Var_16);
    } else if ((Var_16 > 127) && (Var_16 < 2048)) {
      Var_13 += String['fromCharCode'](((Var_16 >> 6) | 192));
      Var_13 += String['fromCharCode'](((Var_16 & 63) | 128));
    } else {
      Var_13 += String['fromCharCode'](((Var_16 >> 12) | 224));
      Var_13 += String['fromCharCode']((((Var_16 >> 6) & 63) | 128));
      Var_13 += String['fromCharCode'](((Var_16 & 63) | 128));
    }
  }
  let Var_17 = Var_13.length;
  let Var_18 = [];
  for (let i = 0; i <= 255; i++) {
    Var_18[i] = Var_10[(i % Var_11)].charCodeAt();
  }
  let Var_19 = [];
  for (let Var_04 = 0;
    (Var_04 < 256); Var_04++) {
    Var_19.push(Var_04);
  }
  for (let Var_20 = 0, Var_04 = 0;
    (Var_04 < 256); Var_04++) {
    Var_20 = (((Var_20 + Var_19[Var_04]) + Var_18[Var_04]) % 256);
    let Var_21 = Var_19[Var_04];
    Var_19[Var_04] = Var_19[Var_20];
    Var_19[Var_20] = Var_21;
  }
  let Var_22 = '';
  for (let Var_23 = 0, Var_20 = 0, Var_04 = 0;
    (Var_04 < Var_17); Var_04++) {
    let Var_24 = '0|2|4|3|5|1'.split('|'),
      Var_25 = 0;
    while (true) {
      switch (Var_24[Var_25++]) {
        case '0':
          Var_23 = ((Var_23 + 1) % 256);
          continue;
        case '1':
          Var_22 += String.fromCharCode(Var_13[Var_04].charCodeAt() ^ Var_19[((Var_19[Var_23] + Var_19[Var_20]) % 256)]);
          continue;
        case '2':
          Var_20 = ((Var_20 + Var_19[Var_23]) % 256);
          continue;
        case '3':
          Var_19[Var_23] = Var_19[Var_20];
          continue;
        case '4':
          var Var_21 = Var_19[Var_23];
          continue;
        case '5':
          Var_19[Var_20] = Var_21;
          continue;
      }
      break;
    }
  }
  let Var_26 = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=';
  for (var Var_27, Var_28, Var_29 = 0, Var_30 = Var_26, Var_31 = ''; Var_22.charAt((Var_29 | 0)) || (Var_30 = '=', (Var_29 % 1)); Var_31 += Var_30.charAt((63 & (Var_27 >> (8 - ((Var_29 % 1) * 8)))))) {
    Var_28 = Var_22['charCodeAt'](Var_29 += 0.75);
    Var_27 = ((Var_27 << 8) | Var_28);
  }
  Var_22 = (nowMD5 + Var_31.replace(/=/g, '')).replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '.');
  return (('data=' + Var_22) + '&v=2');
};

function generateUUID() {
  var d = new Date().getTime();
  var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    var r = (d + Math.random()*16)%16 | 0;
    d = Math.floor(d/16);
    return (c=='x' ? r : (r&0x3|0x8)).toString(16);
  });
  return uuid;
};
function generateMiGu2Param(keyword){
    const sid = (generateUUID() + generateUUID()).replace(/-/g, '');
    const deviceId = md5(generateUUID().replace(/-/g, '')).toLocaleUpperCase();
    const timestamp = new Date().getTime();
    const signature_md5 = '6cdc72a439cef99a3418d2a78aa28c73';
    const text = `${keyword + signature_md5}yyapp2d16148780a1dcc7408e06336b98cfd50${deviceId}${timestamp}`;
    const sign = md5(text);
    return sid+"$"+deviceId+"$"+sign+"$"+timestamp;
};