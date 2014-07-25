BTPingPong
==========

このアプリは，東京工業大学大学院の[ソフトウェア開発演習](http://www.psg.cs.titech.ac.jp/sdl/)という授業の最終課題のために作られました。

何をするアプリか
----

Bluetooth通信（SPPプロファイル）により，2台の端末でPingPongの対戦が可能です。

使い方
----

2台の端末でBTPingPongを起動し，ActionBarから片方の端末でListen，もう片方の端末でConnectします。
Connectした側でデバイス一覧画面が開くので，対戦相手のデバイスをタップで選択します。

この段階でNetworkの状態が「Client」もしくは「Server」になれば準備完了です。
ActionBarから「Start」すると，対戦がスタートします。
どちらかがボール（？）を落とすとラウンドが終了します。
再度Startすることにより何度でも対戦が可能です。

発表資料
----

[20140725SDL.pdf](https://github.com/tondol/BTPingPong/raw/master/20140725SDL.pdf)

既知の不具合
----

- まだペアリング未設定のデバイス同士で対戦しようとすると失敗する

使用素材
----

- 画像: [ラブライブ！公式サイト](http://news.lovelive-anime.jp/app-def/S-102/news/?p=5674)
- 効果音: [TAM Music Factory](http://www.tam-music.com/index.html)
