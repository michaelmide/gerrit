// Copyright 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.gerrit.client;

import com.google.gerrit.client.account.SignInResult;
import com.google.gerrit.client.account.SignInResult.Status;
import com.google.gerrit.client.rpc.GerritCallback;
import com.google.gerrit.client.ui.AutoCenterDialogBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwtjsonrpc.client.CallbackHandle;

/**
 * Prompts the user to sign in to their account.
 * <p>
 * This dialog performs the login within an iframe, allowing normal HTML based
 * login pages to be used, including those which aren't served from the same
 * server as Gerrit. This is important to permit an OpenID provider or some
 * other web based single-sign-on system to be used for authentication.
 * <p>
 * Post login the iframe content is expected to execute the JavaScript snippet:
 * 
 * <pre>
 * $callback(account);
 * </pre>
 * 
 * where <code>$callback</code> is the parameter in the initial request and
 * <code>account</code> is either <code>!= null</code> (the user is now signed
 * in) or <code>null</code> (the sign in was aborted/canceled before it
 * completed).
 */
public class SignInDialog extends AutoCenterDialogBox {
  public static final String SIGNIN_MODE_PARAM = "gerrit.signin_mode";
  public static final String GERRIT_TOKEN = "gerrit.token";

  public static enum Mode {
    SIGN_IN, LINK_IDENTIY;
  }

  private static SignInDialog current;

  private final Mode mode;
  private final CallbackHandle<SignInResult> signInCallback;
  private final AsyncCallback<?> appCallback;
  private final Frame loginFrame;

  /**
   * Create a new dialog to handle user sign in.
   * 
   * @param callback optional; onSuccess will be called if sign is completed.
   *        This can be used to trigger sending an RPC or some other action.
   */
  public SignInDialog(final AsyncCallback<?> callback) {
    this(Mode.SIGN_IN, callback);
  }

  /**
   * Create a new dialog to handle user sign in.
   * 
   * @param signInMode type of mode the login will perform.
   * @param callback optional; onSuccess will be called if sign is completed.
   *        This can be used to trigger sending an RPC or some other action.
   */
  public SignInDialog(final Mode signInMode, final AsyncCallback<?> callback) {
    super(/* auto hide */true, /* modal */true);

    mode = signInMode;
    signInCallback =
        com.google.gerrit.client.account.Util.LOGIN_SVC
            .signIn(new GerritCallback<SignInResult>() {
              public void onSuccess(final SignInResult result) {
                onCallback(result);
              }
            });
    appCallback = callback;

    loginFrame = new Frame();
    onResize(Window.getClientWidth(), Window.getClientHeight());
    add(loginFrame);
    switch (mode) {
      case LINK_IDENTIY:
        setText(Gerrit.C.linkIdentityDialogTitle());
        break;
      default:
        setText(Gerrit.C.signInDialogTitle());
        break;
    }
  }

  @Override
  protected void onResize(final int width, final int height) {
    resizeFrame(width, height);
    super.onResize(width, height);
  }

  private void resizeFrame(final int width, final int height) {
    final int w = Math.min(630, width - 15);
    final int h = Math.min(440, height - 60);
    loginFrame.setWidth(w + "px");
    loginFrame.setHeight(h + "px");
  }

  @Override
  public void show() {
    if (current != null) {
      current.hide();
    }

    super.show();

    current = this;
    signInCallback.install();

    final StringBuilder url = new StringBuilder();
    url.append(GWT.getModuleBaseURL());
    url.append("login");
    url.append("?openid.mode=gerrit.chooseProvider");
    url.append("&callback=parent." + signInCallback.getFunctionName());

    url.append("&");
    url.append(SIGNIN_MODE_PARAM);
    url.append("=");
    url.append(mode.name());

    url.append("&");
    url.append(GERRIT_TOKEN);
    url.append("=");
    url.append(URL.encodeComponent(History.getToken()));

    loginFrame.setUrl(url.toString());
  }

  @Override
  protected void onUnload() {
    if (current == this) {
      signInCallback.cancel();
      current = null;
    }
    super.onUnload();
  }

  private void onCallback(final SignInResult result) {
    final Status rc = result.getStatus();
    if (rc == SignInResult.Status.CANCEL) {
      hide();
    } else if (rc == SignInResult.Status.SUCCESS) {
      onSuccess(result);
    } else {
      GWT.log("Unexpected SignInResult.Status " + rc, null);
    }
  }

  private void onSuccess(final SignInResult result) {
    Gerrit.postSignIn(result.getAccount());
    hide();
    final AsyncCallback<?> ac = appCallback;
    if (ac != null) {
      DeferredCommand.addCommand(new Command() {
        public void execute() {
          ac.onSuccess(null);
        }
      });
    }
  }
}
