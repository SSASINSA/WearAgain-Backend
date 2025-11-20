(function () {
  const EVENT_NAME = 'swagger-ui-ready';
  const ADMIN_STORAGE_KEY = 'swagger-login-admin-token';
  const USER_STORAGE_KEY = 'swagger-login-user-token';
  const KAKAO_POPUP_URL = '/swagger-login/kakao-start.html';
  const MESSAGE_SUCCESS = 'SWAGGER_KAKAO_LOGIN_SUCCESS';
  const MESSAGE_ERROR = 'SWAGGER_KAKAO_LOGIN_ERROR';

  const state = {
    ui: null,
    panel: null,
    adminToken: null,
    userToken: null,
    toastTimer: null,
    statusTimer: null,
    expireTimer: null
  };

  function loadStoredToken(key) {
    try {
      const stored = window.localStorage.getItem(key);
      if (!stored) {
        return null;
      }
      const parsed = JSON.parse(stored);
      if (!parsed.token || !parsed.expiresAt) {
        return null;
      }
      return parsed;
    } catch (error) {
      console.warn('저장된 토큰을 읽는 중 오류가 발생했습니다.', error);
      return null;
    }
  }

  function saveToken(key, token, expiresInSeconds) {
    const expiresAt = Date.now() + (expiresInSeconds * 1000);
    window.localStorage.setItem(key, JSON.stringify({ token: token, expiresAt: expiresAt }));
    return { token: token, expiresAt: expiresAt };
  }

  function removeToken(key) {
    window.localStorage.removeItem(key);
  }

  function formatRemaining(expiresAt) {
    const remaining = expiresAt - Date.now();
    if (remaining <= 0) {
      return '만료됨';
    }
    const totalSeconds = Math.floor(remaining / 1000);
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;
    if (minutes > 0) {
      return minutes + '분 ' + seconds + '초 남음';
    }
    return seconds + '초 남음';
  }

  function getStatusText(token) {
    if (!token) {
      return '토큰 없음';
    }
    return formatRemaining(token.expiresAt);
  }

  function showToast(message, type) {
    if (!state.panel) {
      return;
    }
    let toast = state.panel.querySelector('.swagger-login-toast');
    if (!toast) {
      toast = document.createElement('div');
      toast.className = 'swagger-login-toast';
      state.panel.appendChild(toast);
    }
    toast.textContent = message;
    toast.dataset.type = type || 'info';
    toast.classList.add('visible');
    clearTimeout(state.toastTimer);
    state.toastTimer = setTimeout(function () {
      toast.classList.remove('visible');
    }, 4000);
  }

  function insertStyles() {
    if (document.getElementById('swagger-login-style')) {
      return;
    }
    const style = document.createElement('style');
    style.id = 'swagger-login-style';
    style.textContent = '' +
      '.swagger-login-panel{border:1px solid #d9d9d9;border-radius:8px;padding:16px;margin-bottom:24px;background:#fefefe;box-shadow:0 1px 2px rgba(15,23,42,0.08);font-family:-apple-system,BlinkMacSystemFont,\"Segoe UI\",Roboto,sans-serif;}' +
      '.swagger-login-panel h3{margin:0 0 12px;font-size:18px;font-weight:600;color:#111827;display:flex;align-items:center;gap:8px;}' +
      '.swagger-login-panel h3 span{font-size:13px;font-weight:500;color:#6b7280;}' +
      '.swagger-login-actions{display:flex;flex-wrap:wrap;gap:8px;margin-bottom:12px;}' +
      '.swagger-login-panel button{border:none;border-radius:6px;padding:8px 14px;font-size:14px;font-weight:600;cursor:pointer;transition:background 0.2s,opacity 0.2s;}' +
      '.swagger-login-panel button.primary{background:#111827;color:#fff;}' +
      '.swagger-login-panel button.secondary{background:#f3f4f6;color:#111827;}' +
      '.swagger-login-panel button.danger{background:#dc2626;color:#fff;}' +
      '.swagger-login-panel button:disabled{opacity:0.4;cursor:not-allowed;}' +
      '.swagger-login-status{display:grid;grid-template-columns:repeat(auto-fit,minmax(160px,1fr));gap:8px;font-size:13px;color:#374151;}' +
      '.swagger-login-status div{background:#f9fafb;border:1px solid #e5e7eb;border-radius:6px;padding:8px 12px;}' +
      '.swagger-login-status strong{display:block;font-size:12px;color:#6b7280;margin-bottom:4px;}' +
      '.swagger-login-toast{margin-top:12px;padding:10px 14px;border-radius:6px;font-size:13px;line-height:1.4;background:#eef2ff;color:#1f2937;opacity:0;transform:translateY(-4px);transition:opacity 0.2s,transform 0.2s;}' +
      '.swagger-login-toast.visible{opacity:1;transform:translateY(0);}' +
      '.swagger-login-toast[data-type="error"]{background:#fee2e2;color:#b91c1c;}' +
      '.swagger-login-toast[data-type="success"]{background:#dcfce7;color:#166534;}';
    document.head.appendChild(style);
  }

  function mountPanel() {
    if (document.getElementById('swagger-login-panel')) {
      state.panel = document.getElementById('swagger-login-panel');
      return;
    }
    const infoContainer = document.querySelector('.swagger-ui .information-container');
    if (!infoContainer || !infoContainer.parentElement) {
      return;
    }
    insertStyles();
    const panel = document.createElement('section');
    panel.id = 'swagger-login-panel';
    panel.className = 'swagger-login-panel';
    panel.innerHTML = '' +
      '<h3>테스트 로그인 <span>Swagger Authorization 자동 주입</span></h3>' +
      '<div class="swagger-login-actions">' +
      '  <button type="button" class="primary" data-action="admin">Admin 로그인</button>' +
      '  <button type="button" class="secondary" data-action="user">User (Kakao) 로그인</button>' +
      '  <button type="button" class="danger" data-action="logout">Logout</button>' +
      '</div>' +
      '<div class="swagger-login-status">' +
      '  <div><strong>Admin 토큰</strong><span data-status="admin">토큰 없음</span></div>' +
      '  <div><strong>User 토큰</strong><span data-status="user">토큰 없음</span></div>' +
      '</div>';
    infoContainer.parentElement.insertBefore(panel, infoContainer);
    panel.addEventListener('click', function (event) {
      if (!(event.target instanceof HTMLElement)) {
        return;
      }
      const action = event.target.getAttribute('data-action');
      if (!action) {
        return;
      }
      if (action === 'admin') {
        handleAdminLogin(event.target);
      } else if (action === 'user') {
        handleUserLogin(event.target);
      } else if (action === 'logout') {
        handleLogout();
      }
    });
    state.panel = panel;
    updateStatus();
  }

  function markButtonLoading(button, isLoading) {
    if (!button) {
      return;
    }
    if (isLoading) {
      button.dataset.originalText = button.textContent;
      button.textContent = '처리 중...';
      button.disabled = true;
    } else {
      button.textContent = button.dataset.originalText || button.textContent;
      button.disabled = false;
    }
  }

  function handleAdminLogin(button) {
    markButtonLoading(button, true);
    fetch('/swagger-login/admin/token', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'same-origin'
    })
      .then(function (response) {
        if (!response.ok) {
          throw new Error('관리자 토큰 발급에 실패했습니다. (' + response.status + ')');
        }
        return response.json();
      })
      .then(function (data) {
        state.adminToken = saveToken(ADMIN_STORAGE_KEY, data.accessToken, data.expiresIn || 0);
        preauthorize('adminJWT', data.accessToken);
        updateStatus();
        persistAuthorization();
        showToast('관리자 토큰을 세팅했습니다.', 'success');
      })
      .catch(function (error) {
        console.error('Admin 로그인 실패', error);
        showToast(error.message || '관리자 로그인 실패', 'error');
      })
      .finally(function () {
        markButtonLoading(button, false);
      });
  }

  function openPopup() {
    const width = 520;
    const height = 720;
    const left = window.screenX + (window.outerWidth - width) / 2;
    const top = window.screenY + (window.outerHeight - height) / 2;
    const popup = window.open(
      KAKAO_POPUP_URL,
      'swaggerKakaoLogin',
      'width=' + width + ',height=' + height + ',left=' + left + ',top=' + top + ',resizable=yes,scrollbars=yes'
    );
    if (!popup) {
      showToast('팝업을 차단 해제해 주세요.', 'error');
      return null;
    }
    popup.focus();
    return popup;
  }

  function handleUserLogin(button) {
    markButtonLoading(button, true);
    const popup = openPopup();
    if (!popup) {
      markButtonLoading(button, false);
      return;
    }
    const cleanup = function () {
      markButtonLoading(button, false);
    };
    const timer = setInterval(function () {
      if (popup.closed) {
        clearInterval(timer);
        cleanup();
        showToast('로그인이 취소되었습니다.', 'error');
      }
    }, 500);
    popup.addEventListener('unload', function () {
      setTimeout(function () {
        clearInterval(timer);
        cleanup();
      }, 300);
    });
  }

  function handleLogout() {
    state.adminToken = null;
    state.userToken = null;
    removeToken(ADMIN_STORAGE_KEY);
    removeToken(USER_STORAGE_KEY);
    ['adminJWT', 'userJWT'].forEach(function (scheme) {
      try {
        if (state.ui && state.ui.authActions && typeof state.ui.authActions.logout === 'function') {
          state.ui.authActions.logout([scheme]);
        } else if (state.ui && typeof state.ui.getSystem === 'function') {
          const system = state.ui.getSystem();
          if (system && system.authActions && typeof system.authActions.logout === 'function') {
            system.authActions.logout([scheme]);
          }
        }
      } catch (error) {
        console.warn('Swagger authorization 초기화 중 오류', error);
      }
    });
    window.localStorage.removeItem('authorized');
    updateStatus();
    showToast('모든 토큰을 초기화했습니다.', 'success');
  }

  function preauthorize(scheme, token) {
    if (!state.ui || !window.ui || typeof window.ui.preauthorizeApiKey !== 'function') {
      return;
    }
    try {
      window.ui.preauthorizeApiKey(scheme, token);
      persistAuthorization();
    } catch (error) {
      console.warn('Swagger Authorization 세팅 중 오류', error);
    }
  }

  function persistAuthorization() {
    if (!state.ui || typeof state.ui.getSystem !== 'function') {
      return;
    }
    const system = state.ui.getSystem();
    if (!system || !system.authSelectors || typeof system.authSelectors.authorized !== 'function') {
      return;
    }
    const authorized = system.authSelectors.authorized();
    if (!authorized) {
      return;
    }
    const plain = typeof authorized.toJS === 'function' ? authorized.toJS() : authorized;
    if (!plain || Object.keys(plain).length === 0) {
      window.localStorage.removeItem('authorized');
      return;
    }
    window.localStorage.setItem('authorized', JSON.stringify(plain));
  }

  function updateStatus() {
    if (!state.panel) {
      return;
    }
    const adminStatus = state.panel.querySelector('[data-status="admin"]');
    const userStatus = state.panel.querySelector('[data-status="user"]');
    if (adminStatus) {
      adminStatus.textContent = getStatusText(validToken(state.adminToken));
    }
    if (userStatus) {
      userStatus.textContent = getStatusText(validToken(state.userToken));
    }
  }

  function validToken(token) {
    if (!token) {
      return null;
    }
    if (token.expiresAt && token.expiresAt <= Date.now()) {
      return null;
    }
    return token;
  }

  function handleMessage(event) {
    if (event.origin !== window.location.origin || !event.data || typeof event.data !== 'object') {
      return;
    }
    if (event.data.type === MESSAGE_SUCCESS) {
      state.userToken = saveToken(USER_STORAGE_KEY, event.data.token, event.data.expiresIn || 0);
      preauthorize('userJWT', event.data.token);
      updateStatus();
      persistAuthorization();
      showToast('사용자 토큰을 세팅했습니다.', 'success');
    } else if (event.data.type === MESSAGE_ERROR) {
      showToast(event.data.message || '카카오 로그인 실패', 'error');
    }
  }

  function restoreTokens() {
    state.adminToken = validToken(loadStoredToken(ADMIN_STORAGE_KEY));
    state.userToken = validToken(loadStoredToken(USER_STORAGE_KEY));
    if (state.adminToken) {
      preauthorize('adminJWT', state.adminToken.token);
    }
    if (state.userToken) {
      preauthorize('userJWT', state.userToken.token);
    }
    updateStatus();
    persistAuthorization();
  }

  function initialize(ui) {
    state.ui = ui;
    restoreTokens();
    mountPanel();
    window.addEventListener('message', handleMessage, false);
    const observer = new MutationObserver(function () {
      if (!document.getElementById('swagger-login-panel')) {
        mountPanel();
      }
    });
    observer.observe(document.body, { childList: true, subtree: true });
    if (state.statusTimer) {
      clearInterval(state.statusTimer);
    }
    state.statusTimer = setInterval(updateStatus, 1000);
    if (state.expireTimer) {
      clearInterval(state.expireTimer);
    }
    state.expireTimer = setInterval(function () {
      const adminValid = validToken(state.adminToken);
      const userValid = validToken(state.userToken);
      if (!adminValid && state.adminToken) {
        state.adminToken = null;
        removeToken(ADMIN_STORAGE_KEY);
        persistAuthorization();
      }
      if (!userValid && state.userToken) {
        state.userToken = null;
        removeToken(USER_STORAGE_KEY);
        persistAuthorization();
      }
      updateStatus();
    }, 10000);
  }

  function ensureInitialized() {
    if (state.ui || !window.ui) {
      return;
    }
    initialize(window.ui);
  }

  document.addEventListener(EVENT_NAME, function (event) {
    const detail = event && event.detail ? event.detail : null;
    const ui = detail && detail.ui ? detail.ui : null;
    if (!ui) {
      return;
    }
    initialize(ui);
  });

  if (document.readyState === 'complete' || document.readyState === 'interactive') {
    ensureInitialized();
  } else {
    document.addEventListener('DOMContentLoaded', ensureInitialized);
  }

  window.addEventListener('load', ensureInitialized);
})();
