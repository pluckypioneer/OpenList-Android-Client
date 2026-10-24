package io.openlist.client.core.network

import java.io.IOException

class InsecureTransportException : IOException("已阻止明文 HTTP，请为服务器启用 HTTPS 后重新添加实例")
