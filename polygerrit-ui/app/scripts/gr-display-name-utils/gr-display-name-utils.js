/**
 * @license
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
(function(window) {
  'use strict';

  if (window.GrDisplayNameUtils) {
    return;
  }

  const ANONYMOUS_NAME = 'Anonymous';

  class GrDisplayNameUtils {
    /**
     * enableEmail when true enables to fallback to using email if
     * the account name is not avilable.
     */
    static getUserName(config, account, enableEmail) {
      if (account && account.name) {
        return account.name;
      } else if (account && account.username) {
        return account.username;
      } else if (enableEmail && account && account.email) {
        return account.email;
      } else if (config && config.user &&
          config.user.anonymous_coward_name !== 'Anonymous Coward') {
        return config.user.anonymous_coward_name;
      }

      return ANONYMOUS_NAME;
    }

    static getAccountDisplayName(config, account, enableEmail) {
      const reviewerName = this._accountOrAnon(config, account, enableEmail);
      const reviewerEmail = this._accountEmail(account.email);
      const reviewerStatus = account.status ? '(' + account.status + ')' : '';
      return [reviewerName, reviewerEmail, reviewerStatus]
          .filter(p => p.length > 0).join(' ');
    }

    static _accountOrAnon(config, reviewer, enableEmail) {
      return this.getUserName(config, reviewer, !!enableEmail);
    }

    static _accountEmail(email) {
      if (typeof email !== 'undefined') {
        return '<' + email + '>';
      }
      return '';
    }

    static getGroupDisplayName(group) {
      return group.name + ' (group)';
    }
  }

  window.GrDisplayNameUtils = GrDisplayNameUtils;
})(window);
