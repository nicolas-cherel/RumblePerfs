

extern crate "rustc-serialize" as serialize;
extern crate rumblebars;
extern crate test;

use std::fs::File;
use std::io::Read;
use test::Bencher;

#[bench]
fn compile(b: &mut Bencher) {
  let mut f = File::open("../data/template.hbs").unwrap();
  let mut s = String::new();
  f.read_to_string(&mut s);

  b.iter(|| {
    s.parse::<rumblebars::Template>()
  })
}

#[bench]
fn compile_helper(b: &mut Bencher) {
  let mut f = File::open("../data/helper.hbs").unwrap();
  let mut s = String::new();
  f.read_to_string(&mut s);

  b.iter(|| {
    s.parse::<rumblebars::Template>()
  })
}

#[bench]
fn expansion(b: &mut Bencher) {
    let t = {
      let mut f = File::open("../data/template.hbs").unwrap();
      let mut s = String::new();
      f.read_to_string(&mut s);
      s.parse().unwrap_or_else(|e| {
        println!("cannot parse template");
        panic!(format!("{:?}", e))
      })
    };

    let j = {
      let mut f = File::open("../data/template.json").unwrap();
      let mut s = String::new();
      f.read_to_string(&mut s);
      s.parse::<::serialize::json::Json>().unwrap_or_else(|e| {
        println!("cannot parse json data");
        panic!(format!("{:?}", e))
      })
    };

    
    b.iter(|| {
      let mut vec = Vec::<u8>::new();
      let mut out = ::std::io::BufWriter::new(&mut vec as &mut ::std::io::Write);
      ::rumblebars::eval(&t, &j, &mut out, &::std::default::Default::default())  
    })
}

#[bench]
fn helper_expansion(b: &mut Bencher) {
    let t = {
      let mut f = File::open("../data/helper.hbs").unwrap();
      let mut s = String::new();
      f.read_to_string(&mut s);
      s.parse().unwrap_or_else(|e| {
        println!("cannot parse template");
        panic!(format!("{:?}", e))
      })
    };

    let j = {
      let mut f = File::open("../data/template.json").unwrap();
      let mut s = String::new();
      f.read_to_string(&mut s);
      s.parse::<::serialize::json::Json>().unwrap_or_else(|e| {
        println!("cannot parse json data");
        panic!(format!("{:?}", e))
      })
    };

    
    b.iter(|| {
      let mut vec = Vec::<u8>::new();
      let mut out = ::std::io::BufWriter::new(&mut vec as &mut ::std::io::Write);
      ::rumblebars::eval(&t, &j, &mut out, &::std::default::Default::default())
    })
}
