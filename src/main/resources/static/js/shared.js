/* global htmx */

let _deletedProductTitle = '';

function showDeleteDialog(button) {
    const productId = button.getAttribute('data-product-id');
    const productTitle = button.getAttribute('data-product-title');
    const dialog = document.getElementById('deleteDialog');
    const form = document.getElementById('deleteForm');

    document.getElementById('deleteProductTitle').textContent = productTitle;

    const container = document.getElementById('product-table-container');
    const select = container ? container.querySelector('#pageSize') : null;
    const sortBy = select ? select.getAttribute('data-sort-by') || 'id' : 'id';
    const order = select ? select.getAttribute('data-order') || 'asc' : 'asc';
    const pageSize = select ? select.value || '10' : '10';
    const searchInput = document.getElementById('searchInput');
    const query = searchInput ? searchInput.value : '';

    let deleteUrl = '/products/' + productId + '/delete?sortBy=' + encodeURIComponent(sortBy)
        + '&order=' + encodeURIComponent(order) + '&page=0&pageSize=' + encodeURIComponent(pageSize);
    if (query) {
        deleteUrl += '&query=' + encodeURIComponent(query);
    }

    form.action = deleteUrl;
    form.setAttribute('hx-post', deleteUrl);
    form.setAttribute('hx-target', '#product-table-container');
    form.setAttribute('hx-swap', 'innerHTML');

    dialog.open = true;
}

function handleDeleteSuccess(event) {
    if (event.detail?.target?.id !== 'product-table-container') return false;
    const requestPath = event.detail?.pathInfo?.requestPath;
    if (event.detail?.xhr?.status !== 200 || !requestPath || !requestPath.includes('/delete')) return false;

    const deleteDialog = document.getElementById('deleteDialog');
    if (deleteDialog) deleteDialog.open = false;

    if (_deletedProductTitle) {
        const successDialog = document.getElementById('deleteSuccessDialog');
        if (successDialog) {
            document.getElementById('deletedProductTitle').textContent = _deletedProductTitle;
            setTimeout(function () { successDialog.open = true; }, 100);
            _deletedProductTitle = '';
        }
    }
    return true;
}

function initSharedHandlers() {
    document.body.addEventListener('submit', function (e) {
        if (e.target.id === 'deleteForm') {
            e.preventDefault();
            htmx.ajax('POST', e.target.action, {
                target: '#product-table-container',
                swap: 'innerHTML'
            });
        }
    });

    document.body.addEventListener('change', function (event) {
        let select = event.target;
        if (select.tagName !== 'WA-SELECT') {
            select = select.closest('wa-select') || document.getElementById('pageSize');
        }
        if (!select || select.id !== 'pageSize') return;

        const sortBy = select.getAttribute('data-sort-by') || 'id';
        const order = select.getAttribute('data-order') || 'asc';
        const newSize = select.value;
        const searchInput = document.getElementById('searchInput');
        const query = searchInput ? (searchInput.value || '') : '';

        let url;
        if (query) {
            url = '/products/search?query=' + encodeURIComponent(query)
                + '&sortBy=' + encodeURIComponent(sortBy) + '&order=' + encodeURIComponent(order)
                + '&page=0&pageSize=' + encodeURIComponent(newSize);
        } else {
            url = '/products?sortBy=' + encodeURIComponent(sortBy)
                + '&order=' + encodeURIComponent(order)
                + '&page=0&pageSize=' + encodeURIComponent(newSize);
        }

        htmx.ajax('GET', url, {
            target: '#product-table-container',
            swap: 'innerHTML'
        });
    });

    document.body.addEventListener('htmx:beforeRequest', function (event) {
        const requestPath = event.detail?.pathInfo?.requestPath;
        if (requestPath && requestPath.includes('/delete')) {
            const el = document.getElementById('deleteProductTitle');
            if (el) _deletedProductTitle = el.textContent;
        }
    });

    document.body.addEventListener('htmx:beforeRequest', function () {
        document.body.classList.add('htmx-request');
    });

    document.body.addEventListener('htmx:afterRequest', function () {
        document.body.classList.remove('htmx-request');
    });
}
