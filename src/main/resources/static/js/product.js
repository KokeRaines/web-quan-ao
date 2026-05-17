export function initProductSort() {
    const sortLinks = document.querySelectorAll('.price-sort');
    const container = document.querySelector('.row.justify-content-center');
    const priceBtn = document.getElementById('priceDropdownButton');

    if (!container || sortLinks.length === 0) return;

    sortLinks.forEach(btn => {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            const sortOrder = this.dataset.sort;
            
            // Lấy tất cả các thẻ bao ngoài sản phẩm
            const items = Array.from(container.querySelectorAll('.col-md-4.mb-4'));
			// Dòng console.log bạn cần đây:
			console.log("Danh sách sản phẩm tìm thấy:", items);
			console.log("Thứ tự sắp xếp được chọn:", sortOrder);
            items.sort((a, b) => {
                const getPrice = (el) => {
                    const priceText = el.querySelector('.new-price')?.innerText || '';
                    
                    if (priceText.includes('COMING SOON')) {
                        return -1; // Đánh dấu đặc biệt cho hàng sắp về
                    }

                    // Xử lý chuỗi "1.200.000 VND" -> "1200000"
                    // Loại bỏ tất cả ký tự không phải là số
                    const cleanPrice = priceText.replace(/\D/g, ''); 
                    return parseInt(cleanPrice) || 0;
                };

                const priceA = getPrice(a);
                const priceB = getPrice(b);

                // Luôn đẩy COMING SOON xuống cuối danh sách
                if (priceA === -1) return 1;
                if (priceB === -1) return -1;

                return sortOrder === 'asc' ? priceA - priceB : priceB - priceA;
            });

            // Xóa nội dung cũ và chèn lại theo thứ tự mới
            container.innerHTML = '';
            items.forEach(item => container.appendChild(item));

            // Cập nhật text trên button để người dùng biết đang lọc gì
            if (priceBtn) {
                priceBtn.innerText = sortOrder === 'asc' ? 'Giá: Thấp đến cao' : 'Giá: Cao đến thấp';
            }
        });
    });
}