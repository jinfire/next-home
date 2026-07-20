self.addEventListener('push', (event) => {
  let payload = { title: 'Next Home', body: '갈아타기 조건을 확인해 보세요.', url: '/' }
  if (event.data) {
    try {
      payload = { ...payload, ...event.data.json() }
    } catch {
      payload.body = event.data.text()
    }
  }
  event.waitUntil(self.registration.showNotification(payload.title, {
    body: payload.body,
    icon: '/favicon.svg',
    badge: '/favicon.svg',
    tag: 'next-home-upgrade-alert',
    data: { url: payload.url },
  }))
})

self.addEventListener('notificationclick', (event) => {
  event.notification.close()
  const target = event.notification.data?.url || '/'
  event.waitUntil(clients.matchAll({ type: 'window', includeUncontrolled: true }).then((windows) => {
    const current = windows.find((windowClient) => windowClient.url.includes(self.location.origin))
    return current ? current.focus() : clients.openWindow(target)
  }))
})
