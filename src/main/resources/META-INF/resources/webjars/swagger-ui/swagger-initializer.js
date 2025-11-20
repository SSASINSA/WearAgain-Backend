(function () {
  const DEFAULT_SPEC_URL = '../v3/api-docs';
  const DEFAULT_CONFIG_URL = '../v3/api-docs/swagger-config';

  function resolveConfigUrl() {
    const params = new URLSearchParams(window.location.search);
    return params.get('configUrl') || DEFAULT_CONFIG_URL;
  }

  function mergeConfig(config) {
    return {
      url: config.url || DEFAULT_SPEC_URL,
      urls: config.urls,
      dom_id: '#swagger-ui',
      deepLinking: true,
      layout: 'BaseLayout',
      persistAuthorization: true,
      oauth2RedirectUrl: config.oauth2RedirectUrl || window.location.origin + '/swagger-ui/oauth2-redirect.html',
      presets: [SwaggerUIBundle.presets.apis, SwaggerUIStandalonePreset],
      plugins: [SwaggerUIBundle.plugins.DownloadUrl]
    };
  }

  function renderError(message) {
    const container = document.getElementById('swagger-ui');
    if (!container) {
      return;
    }
    container.innerHTML = '<div class="swagger-error">' + message + '</div>';
  }

  function initSwaggerUI(options) {
    try {
      window.ui = SwaggerUIBundle(options);
      document.dispatchEvent(new CustomEvent('swagger-ui-ready', { detail: { ui: window.ui, config: options } }));
    } catch (error) {
      console.error('Swagger UI 초기화 실패', error);
      renderError('Swagger UI를 초기화할 수 없습니다.');
    }
  }

  function loadConfig() {
    const configUrl = resolveConfigUrl();
    return fetch(configUrl, { credentials: 'same-origin' })
      .then(function (response) {
        if (!response.ok) {
          throw new Error('Swagger 설정을 불러오지 못했습니다. (' + response.status + ')');
        }
        return response.json();
      })
      .catch(function (error) {
        console.warn('Swagger 설정을 불러오는 데 실패했습니다. 기본 설정을 사용합니다.', error);
        return {};
      });
  }

  window.addEventListener('load', function () {
    loadConfig()
      .then(mergeConfig)
      .then(initSwaggerUI)
      .catch(function (error) {
        console.error('Swagger UI 초기화 중 예외가 발생했습니다.', error);
        renderError('Swagger UI 초기화 중 오류가 발생했습니다. 콘솔을 확인해 주세요.');
      });
  });
})();
