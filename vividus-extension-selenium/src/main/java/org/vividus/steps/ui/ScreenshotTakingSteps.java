/*
 * Copyright 2019-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vividus.steps.ui;

import java.io.IOException;
import java.nio.file.Path;

import com.google.common.eventbus.EventBus;

import org.jbehave.core.annotations.When;
import org.vividus.reporter.event.AttachmentPublishEvent;
import org.vividus.reporter.model.Attachment;
import org.vividus.selenium.screenshot.ScreenshotTaker;

public class ScreenshotTakingSteps
{
    private final ScreenshotTaker screenshotTaker;
    private final EventBus eventBus;

    public ScreenshotTakingSteps(ScreenshotTaker screenshotTaker, EventBus eventBus)
    {
        this.screenshotTaker = screenshotTaker;
        this.eventBus = eventBus;
    }

    /**
     * Takes a screenshot and publish it to the report
     */
    @When("I take screenshot")
    public void takeScreenshot()
    {
        screenshotTaker.takeScreenshot("Step_Screenshot").ifPresent(screenshot ->
        {
            Attachment attachment = new Attachment(screenshot.getData(), screenshot.getFileName(), "image/png");
            eventBus.post(new AttachmentPublishEvent(attachment));
        });
    }

    /**
     * Takes a screenshot and saves it by the specified <b>path</b>
     * <br>
     * <i>Possible values:</i>
     * <ul>
     * <li>An <b>absolute</b> path contains the root directory and all other
     * subdirectories that contain a file or folder.
     * (<i>C:\Windows\screenshot.bmp</i>)
     * <li>A <b>relative</b> path starts from some given working directory. (<i>screenshot.bmp</i>)
     * </ul>
     * @param path Path to the location for saving the screenshot
     * @throws IOException If an input or output exception occurred
     */
    @When("I take screenshot and save it to folder `$path`")
    public void takeScreenshotToPath(Path path) throws IOException
    {
        screenshotTaker.takeScreenshot(path);
    }
}
