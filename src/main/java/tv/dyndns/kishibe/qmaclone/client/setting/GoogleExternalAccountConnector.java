package tv.dyndns.kishibe.qmaclone.client.setting;

/**
 * Google Identity Servicesを利用してOIDC subjectを取得する実装。
 */
public class GoogleExternalAccountConnector implements ExternalAccountConnector {
  private static final String PROVIDER = "google";

  @Override
  public void authorize(Callback callback) {
    authorizeNative(callback);
  }

  private void onAuthorizeSuccess(Callback callback, String subject) {
    callback.onSuccess(PROVIDER, subject);
  }

  private void onAuthorizeFailure(Callback callback, String message) {
    callback.onFailure(new IllegalStateException(message));
  }

  /**
   * One Tapで取得できない場合はOAuth popupにフォールバックする。
   */
  private native void authorizeNative(Callback callback) /*-{
    var self = this;
    var completed = false;
    var googleObj = $wnd.google;

    var completeSuccess = function(subject) {
      if (completed) {
        return;
      }
      completed = true;
      self.@tv.dyndns.kishibe.qmaclone.client.setting.GoogleExternalAccountConnector::onAuthorizeSuccess(Ltv/dyndns/kishibe/qmaclone/client/setting/ExternalAccountConnector$Callback;Ljava/lang/String;)(callback, subject);
    };

    var completeFailure = function(message) {
      if (completed) {
        return;
      }
      completed = true;
      self.@tv.dyndns.kishibe.qmaclone.client.setting.GoogleExternalAccountConnector::onAuthorizeFailure(Ltv/dyndns/kishibe/qmaclone/client/setting/ExternalAccountConnector$Callback;Ljava/lang/String;)(callback, message);
    };

    var parseSubjectFromJwt = function(jwt) {
      if (!jwt) {
        return null;
      }
      var parts = jwt.split(".");
      if (parts.length < 2) {
        return null;
      }
      var payload = parts[1].replace(/-/g, "+").replace(/_/g, "/");
      while (payload.length % 4 !== 0) {
        payload += "=";
      }
      try {
        var json = JSON.parse($wnd.atob(payload));
        return json && json.sub ? json.sub : null;
      } catch (e) {
        return null;
      }
    };

    if (!googleObj || !googleObj.accounts) {
      completeFailure("Google Identity Services is not loaded.");
      return;
    }

    var clientId = $wnd.QMACloneGoogleClientId;
    if (!clientId) {
      var metas = $doc.getElementsByTagName("meta");
      for (var i = 0; i < metas.length; i++) {
        var meta = metas[i];
        if (meta && meta.name === "google-signin-client_id" && meta.content) {
          clientId = meta.content;
          break;
        }
      }
    }
    if (!clientId) {
      completeFailure("Google Client ID is not configured.");
      return;
    }

    var popupFallback = function(reason) {
      if (!googleObj.accounts.oauth2 || !googleObj.accounts.oauth2.initTokenClient) {
        completeFailure("Google OAuth popup is unavailable: " + reason);
        return;
      }

      var tokenClient = googleObj.accounts.oauth2.initTokenClient({
        client_id : clientId,
        scope : "openid profile email",
        prompt : "select_account",
        callback : function(tokenResponse) {
          if (!tokenResponse || tokenResponse.error || !tokenResponse.access_token) {
            var error = tokenResponse && tokenResponse.error ? tokenResponse.error : "no_access_token";
            completeFailure("Google OAuth failed: " + error);
            return;
          }

          var xhr = new XMLHttpRequest();
          xhr.open("GET", "https://openidconnect.googleapis.com/v1/userinfo", true);
          xhr.setRequestHeader("Authorization", "Bearer " + tokenResponse.access_token);
          xhr.onreadystatechange = function() {
            if (xhr.readyState !== 4) {
              return;
            }
            if (xhr.status < 200 || xhr.status >= 300) {
              completeFailure("Failed to fetch userinfo: HTTP " + xhr.status);
              return;
            }
            try {
              var userInfo = JSON.parse(xhr.responseText);
              if (!userInfo || !userInfo.sub) {
                completeFailure("userinfo response does not contain sub.");
                return;
              }
              completeSuccess(userInfo.sub);
            } catch (e) {
              completeFailure("Failed to parse userinfo response.");
            }
          };
          xhr.send();
        }
      });
      tokenClient.requestAccessToken({
        prompt : "select_account"
      });
    };

    if (!googleObj.accounts.id) {
      popupFallback("id_api_unavailable");
      return;
    }

    googleObj.accounts.id.initialize({
      client_id : clientId,
      callback : function(response) {
        var subject = parseSubjectFromJwt(response && response.credential);
        if (subject) {
          completeSuccess(subject);
        } else {
          popupFallback("missing_subject_in_id_token");
        }
      }
    });

    googleObj.accounts.id.prompt(function(notification) {
      if (completed || !notification) {
        return;
      }
      if (notification.isNotDisplayed && notification.isNotDisplayed()) {
        var reason1 = notification.getNotDisplayedReason ? notification.getNotDisplayedReason() : "not_displayed";
        popupFallback("not_displayed:" + reason1);
        return;
      }
      if (notification.isSkippedMoment && notification.isSkippedMoment()) {
        var reason2 = notification.getSkippedReason ? notification.getSkippedReason() : "skipped";
        popupFallback("skipped:" + reason2);
        return;
      }
      if (notification.isDismissedMoment && notification.isDismissedMoment()) {
        var reason3 = notification.getDismissedReason ? notification.getDismissedReason() : "dismissed";
        popupFallback("dismissed:" + reason3);
      }
    });
  }-*/;
}
