BT-350制御ライブラリパッケージ
                                                  Seiko Epson Corporation Apr/2018

■SDK機能概要
本ライブラリは、BT-350の下記項目を制御する機能を提供します
 - ディスプレイ制御（2D/3D切替、ディスプレイミュート、表示距離）
 - 独自センサー制御
 - オーディオ制御
 - UI制御（キーイベント、タッチセンサー感度、トラックパッド座標モード・座標系回転・操作無効化、LED制御）

■ライブラリ利用環境
 機材：BT-350

■内容
 - BT350Ctrl.jar  　 ライブラリ本体
 - BT350Sample.zip   本ライブラリを使用したサンプルアプリケーションプロジェクト

■ライブラリ使用方法
ライブラリの使用の詳細は、デベロッパーズガイドを参照してください。

■サンプルアプリケーションプロジェクト使用方法
BT350Sample.zipを展開して下さい。
AndroidStudioの[File][Open] より、各プロジェクトのフォルダを指定して下さい。

■サンプルアプリケーションプロジェクト名と機能
 - Sample Audio Control     : マイクゲインのプリセット変更
 - Sample Camera Preview    : 上下キーでカメラのプレビュー解像度切替
 - Sample Controller Gyro   : 3枚の画像をコントローラで切替、取得したセンサ値をTextViewで表示
 - Sample Display Control   : 2D/3D切替、画面ミュート、輝度変更、表示距離制御
 - Sample Headset Gyro      : 3枚の画像を首振りで切替、取得したセンサ値をTextViewで表示
 - Sample HW Key            : 3枚の画像を左右キーで切替
 - Sample Key Control       : キーイベント制御
 - Sample Launcher          : 特定のアプリのみを起動(ランチャー)
 - Sample Led Control       : LEDの制御
 - Sample Show Battery info : バッテリー情報の表示
 - Sample Tap Mute          : タップによる画面ミュート
 - Sample Trackpad Control  : トラックパッド座標モード・座標系回転・操作無効化

■変更履歴
2017/5  新規
2017/9  一部のサンプルアプリケーションのプロジェクトの設定ファイルの不具合を修正
2017/12 Sample Launcherを追加
2018/4  Sample Launcherのバグ修正
