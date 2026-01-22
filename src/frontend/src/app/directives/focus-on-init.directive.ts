import { Directive, ElementRef, AfterViewInit, inject } from '@angular/core';

@Directive({
  selector: '[appFocusOnInit]',
  standalone: true
})

/* This directive is designed to be used within HTML and automatically calls up the keyboard when clicked in mobile view. */

export class FocusOnInitDirective implements AfterViewInit {
  private el = inject(ElementRef);

  ngAfterViewInit() {
    setTimeout(() => {
      this.el.nativeElement.focus();
    }, 100);
  }
}
