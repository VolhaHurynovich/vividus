/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.mobileapp.steps;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;
import org.vividus.mobileapp.action.TouchActions;
import org.vividus.mobileapp.model.SwipeDirection;
import org.vividus.selenium.manager.GenericWebDriverManager;
import org.vividus.steps.ComparisonRule;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.context.IUiContext;

@ExtendWith(MockitoExtension.class)
class TouchStepsTests
{
    private static final Dimension DIMENSION = new Dimension(0, 1184);

    private static final String ELEMENT_TO_TAP = "The element to tap";

    @Mock private IBaseValidations baseValidations;
    @Mock private TouchActions touchActions;
    @Mock private ISearchActions searchActions;
    @Mock private Locator locator;
    @Mock private GenericWebDriverManager genericWebDriverManager;
    @Mock private IUiContext uiContext;
    @InjectMocks private TouchSteps touchSteps;

    @AfterEach
    void afterEach()
    {
        verifyNoMoreInteractions(touchActions, baseValidations, searchActions, locator);
    }

    @Test
    void testTapByLocatorWithDuration()
    {
        WebElement element = mock(WebElement.class);
        when(baseValidations.assertElementExists(ELEMENT_TO_TAP, locator)).thenReturn(Optional.of(element));
        touchSteps.tapByLocatorWithDuration(locator, Duration.ZERO);
        verify(touchActions).tap(element, Duration.ZERO);
    }

    @Test
    void testTapByLocatorWithDurationElementIsEmpty()
    {
        when(baseValidations.assertElementExists(ELEMENT_TO_TAP, locator)).thenReturn(Optional.empty());
        touchSteps.tapByLocatorWithDuration(locator, Duration.ZERO);
    }

    @Test
    void testTapByLocator()
    {
        WebElement element = mock(WebElement.class);
        when(baseValidations.assertElementExists(ELEMENT_TO_TAP, locator)).thenReturn(Optional.of(element));
        touchSteps.tapByLocator(locator);
        verify(touchActions).tap(element);
    }

    @Test
    void testTapByLocatorElementIsEmpty()
    {
        when(baseValidations.assertElementExists(ELEMENT_TO_TAP, locator)).thenReturn(Optional.empty());
        touchSteps.tapByLocator(locator);
    }

    @ParameterizedTest
    @CsvSource({
        "138, 631",
        "1154, 326"
    })
    void shouldSwipeToElement(int elementY, int endY)
    {
        mockScreenSize();
        WebElement element = mock(WebElement.class);
        mockAssertElementsNumber(List.of(element), true);
        when(element.getLocation()).thenReturn(new Point(-1, elementY));
        SearchParameters parameters = initSwipeMocks();

        when(searchActions.findElements(locator)).thenReturn(new ArrayList<>())
                                                 .thenReturn(List.of())
                                                 .thenReturn(List.of(element));
        doAnswer(a ->
        {
            BooleanSupplier condition = a.getArgument(3, BooleanSupplier.class);
            condition.getAsBoolean();
            condition.getAsBoolean();
            return null;
        }).when(touchActions).swipeUntil(eq(SwipeDirection.UP), eq(Duration.ZERO), any(Rectangle.class),
                any(BooleanSupplier.class));

        touchSteps.swipeToElement(SwipeDirection.UP, locator, Duration.ZERO);

        verifyNoMoreInteractions(parameters);
        verify(touchActions).performVerticalSwipe(eq(592), eq(endY), argThat(sa ->
            DIMENSION.equals(sa.getDimension()) && sa.getPoint().equals(new Point(0, 0))), eq(Duration.ZERO));
    }

    @Test
    void shouldSwipeToElementUsingContextElement()
    {
        WebElement element = mock(WebElement.class);
        mockAssertElementsNumber(List.of(element), true);
        when(element.getLocation()).thenReturn(new Point(-1, 138));
        WebElement searchContext = mock(WebElement.class);
        Point point = new Point(10, 10);
        Dimension contextDimension = new Dimension(1920, 1080);
        when(searchContext.getRect()).thenReturn(new Rectangle(point, contextDimension));
        when(uiContext.getSearchContext()).thenReturn(searchContext);

        when(searchActions.findElements(locator)).thenReturn(new ArrayList<>())
                                                 .thenReturn(List.of())
                                                 .thenReturn(List.of(element));
        doAnswer(a ->
        {
            BooleanSupplier condition = a.getArgument(3, BooleanSupplier.class);
            condition.getAsBoolean();
            condition.getAsBoolean();
            return null;
        }).when(touchActions).swipeUntil(eq(SwipeDirection.UP), eq(Duration.ZERO), argThat(sa ->
            contextDimension.equals(sa.getDimension()) && sa.getPoint().equals(point)),
                any(BooleanSupplier.class));
        SearchParameters parameters = initSwipeMocks();

        touchSteps.swipeToElement(SwipeDirection.UP, locator, Duration.ZERO);

        verifyNoMoreInteractions(parameters);
        verify(touchActions).performVerticalSwipe(eq(540), eq(564), argThat(sa ->
            contextDimension.equals(sa.getDimension()) && sa.getPoint().equals(point)), eq(Duration.ZERO));
    }

    @Test
    void shouldSwipeToElementToTryToFindItButThatDoesntExist()
    {
        mockScreenSize();
        mockAssertElementsNumber(List.of(), false);
        initSwipeMocks();
        when(searchActions.findElements(locator)).thenReturn(List.of());
        doAnswer(a ->
        {
            BooleanSupplier condition = a.getArgument(3, BooleanSupplier.class);
            condition.getAsBoolean();
            return null;
        }).when(touchActions).swipeUntil(eq(SwipeDirection.UP), eq(Duration.ZERO), argThat(sa ->
            DIMENSION.equals(sa.getDimension()) && sa.getPoint().equals(new Point(0, 0))), any(BooleanSupplier.class));

        touchSteps.swipeToElement(SwipeDirection.UP, locator, Duration.ZERO);

        verify(touchActions).swipeUntil(eq(SwipeDirection.UP), eq(Duration.ZERO),
                argThat(sa -> DIMENSION.equals(sa.getDimension()) && sa.getPoint().equals(new Point(0, 0))),
                any(BooleanSupplier.class));
        verifyNoMoreInteractions(touchActions);
    }

    @ParameterizedTest
    @ValueSource(strings = {"UP", "DOWN"})
    void shouldNotSwipeToElementIfItAlreadyExists(SwipeDirection swipeDireciton)
    {
        mockScreenSize();
        WebElement element = mock(WebElement.class);
        when(element.getLocation()).thenReturn(new Point(-1, 500));
        mockAssertElementsNumber(List.of(element), true);
        when(searchActions.findElements(locator)).thenReturn(List.of(element));
        SearchParameters parameters = initSwipeMocks();

        touchSteps.swipeToElement(swipeDireciton, locator, Duration.ZERO);

        verifyNoMoreInteractions(parameters);
    }

    @ParameterizedTest
    @ValueSource(strings = {"LEFT", "RIGHT"})
    void shouldNotSwipeToElementIfItAlreadyExistsAndDoNotAdjustItForHorizontalScroll(SwipeDirection swipeDireciton)
    {
        initSwipeMocks();
        WebElement element = mock(WebElement.class);
        mockAssertElementsNumber(List.of(element), true);

        when(searchActions.findElements(locator)).thenReturn(List.of(element));

        touchSteps.swipeToElement(swipeDireciton, locator, Duration.ZERO);
        verifyNoInteractions(genericWebDriverManager);
    }

    private SearchParameters initSwipeMocks()
    {
        SearchParameters parameters = mock(SearchParameters.class);
        when(locator.getSearchParameters()).thenReturn(parameters);
        when(parameters.setWaitForElement(false)).thenReturn(parameters);
        return parameters;
    }

    private void mockAssertElementsNumber(List<WebElement> elements, boolean result)
    {
        when(baseValidations.assertElementsNumber(String.format("The element by locator %s exists", locator),
                elements, ComparisonRule.EQUAL_TO, 1)).thenReturn(result);
    }

    private void mockScreenSize()
    {
        when(genericWebDriverManager.getSize()).thenReturn(DIMENSION);
    }
}
