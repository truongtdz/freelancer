import { Component, EventEmitter, Input, OnChanges, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-pagination',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (totalPages > 1) {
      <div class="flex items-center justify-between mt-6">
        <p class="text-sm text-gray-600">
          Hiển thị {{ rangeStart }}-{{ rangeEnd }} / {{ totalElements }} kết quả
        </p>
        <div class="flex items-center gap-1">
          <button (click)="changePage(currentPage - 1)" [disabled]="currentPage === 0"
                  class="px-3 py-1 rounded border text-sm disabled:opacity-40 hover:bg-gray-100">
            ‹
          </button>
          @for (p of pages; track p) {
            <button (click)="changePage(p)"
                    class="px-3 py-1 rounded border text-sm"
                    [class.bg-indigo-600]="p === currentPage"
                    [class.text-white]="p === currentPage"
                    [class.hover:bg-gray-100]="p !== currentPage">
              {{ p + 1 }}
            </button>
          }
          <button (click)="changePage(currentPage + 1)" [disabled]="currentPage === totalPages - 1"
                  class="px-3 py-1 rounded border text-sm disabled:opacity-40 hover:bg-gray-100">
            ›
          </button>
        </div>
      </div>
    }
  `
})
export class PaginationComponent implements OnChanges {
  @Input() currentPage = 0;
  @Input() totalPages = 0;
  @Input() totalElements = 0;
  @Input() pageSize = 10;
  @Output() pageChange = new EventEmitter<number>();

  pages: number[] = [];

  get rangeStart() { return this.currentPage * this.pageSize + 1; }
  get rangeEnd() { return Math.min((this.currentPage + 1) * this.pageSize, this.totalElements); }

  ngOnChanges() {
    const half = 2;
    const start = Math.max(0, this.currentPage - half);
    const end = Math.min(this.totalPages - 1, this.currentPage + half);
    this.pages = Array.from({ length: end - start + 1 }, (_, i) => start + i);
  }

  changePage(p: number) {
    if (p < 0 || p >= this.totalPages) return;
    this.pageChange.emit(p);
  }
}
