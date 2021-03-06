/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dhbk.android.testsqlbrite;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dhbk.android.testsqlbrite.db.DbModule;

@Module(includes = {DbModule.class,})
public final class TodoModule {
    private final Application application;

    TodoModule(Application application) {
        this.application = application;
    }

    // provide application context
    @Provides
    @Singleton
    Application provideApplication() {
        return application;
    }
}
