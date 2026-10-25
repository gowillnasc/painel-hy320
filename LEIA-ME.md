# Painel HY320 para Android

1. Gerar o APK: abra a pasta no Android Studio (Build > Build APK) ou envie ao GitHub; a aba Actions gera o APK.
2. Instalar no aparelho: pendrive ou `adb install app-debug.apk`.
3. Abra o app uma vez e permita "Sobrepor outros apps".
4. Aperte Home e escolha "Painel HY320" > "Sempre". Ele passa a abrir sozinho ao ligar.
5. Opcional (reduz animacoes do sistema): `adb shell pm grant com.hy320.painel android.permission.WRITE_SECURE_SETTINGS`

Para trocar a tela, edite app/src/main/assets/painel.html.
