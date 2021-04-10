#include <vector>
#include <iostream>

#include <png.h>

static_assert(sizeof(int) == 4);

extern "C" {
int png_read(char* file_name, int width, int height, int* data) {
    auto fp = fopen(file_name, "rb");
    if (fp == nullptr) {
        return -1;
    }
    auto png_ptr = png_create_read_struct(PNG_LIBPNG_VER_STRING, nullptr, nullptr, nullptr);
    if (png_ptr == nullptr) {
        fclose(fp);
        return -1;
    }
    auto info_ptr = png_create_info_struct(png_ptr);
    if (info_ptr == nullptr) {
        png_destroy_read_struct(&png_ptr, nullptr, nullptr);
        fclose(fp);
        return -1;
    }
    if (setjmp(png_jmpbuf(png_ptr))) {
        png_destroy_read_struct(&png_ptr, &info_ptr, nullptr);
        fclose(fp);
        return -1;
    }
    png_init_io(png_ptr, fp);
    auto row_ptrs = std::vector<png_bytep>(height);
    for (auto i = 0; i < height; i++) {
        row_ptrs[i] = (png_bytep)&data[i * width];
    }
    png_set_rows(png_ptr, info_ptr, row_ptrs.data());
    png_read_png(png_ptr, info_ptr, PNG_TRANSFORM_IDENTITY, nullptr);
    png_destroy_read_struct(&png_ptr, &info_ptr, nullptr);
    fclose(fp);
    return 0;
}

int png_write(char* file_name, int width, int height, int* data) {
    auto fp = fopen(file_name, "wb");
    if (fp == nullptr) {
        return -1;
    }
    auto png_ptr = png_create_write_struct(PNG_LIBPNG_VER_STRING, nullptr, nullptr, nullptr);
    if (png_ptr == nullptr) {
        fclose(fp);
        return -1;
    }
    auto info_ptr = png_create_info_struct(png_ptr);
    if (info_ptr == nullptr) {
        png_destroy_write_struct(&png_ptr, nullptr);
        fclose(fp);
        return -1;
    }
    if (setjmp(png_jmpbuf(png_ptr))) {
        png_destroy_write_struct(&png_ptr, &info_ptr);
        fclose(fp);
        return -1;
    }
    png_init_io(png_ptr, fp);
    png_set_IHDR(png_ptr, info_ptr, width, height, 8, PNG_COLOR_TYPE_RGBA, PNG_INTERLACE_NONE, PNG_COMPRESSION_TYPE_BASE, PNG_FILTER_TYPE_BASE);
    auto row_ptrs = std::vector<png_bytep>(height);
    for (auto i = 0; i < height; i++) {
        row_ptrs[i] = (png_bytep)&data[i * width];
    }
    png_set_rows(png_ptr, info_ptr, row_ptrs.data());
    png_write_png(png_ptr, info_ptr, PNG_TRANSFORM_IDENTITY, nullptr);
    png_destroy_write_struct(&png_ptr, &info_ptr);
    fclose(fp);
    return 0;
}
}